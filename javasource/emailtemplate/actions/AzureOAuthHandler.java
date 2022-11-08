package emailtemplate.actions;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.ISession;
import com.mendix.thirdparty.org.json.JSONObject;
import emailtemplate.proxies.*;
import encryption.proxies.microflows.Microflows;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import system.proxies.Session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AzureOAuthHandler implements OAuthHandler {
    static final String ERROR = "error";
    static final String REFRESH_TOKEN = "refresh_token";
    static final String REAUTHACCIDSEPRATOR = "|ec|";
    static final String STATE = "state";
    static final String ACCESS_TOKEN = "access_token";
    static final String ID_TOKEN = "id_token";
    static final String SCOPE = String.join("%20", "email", "profile", "openid", "offline_access", "https://outlook.office.com/SMTP.Send");
    HttpServletResponse servletResponse;
	HttpServletRequest servletRequest;
	ISession sessionFromRequest;
    OAuthProviderDetails providerDetails;
	StringBuilder oauthUrl = new StringBuilder("https://login.microsoftonline.com/");
	final String redirectUri = Core.getConfiguration().getApplicationRootUrl() + "callback/azure";

    AzureOAuthHandler(OAuthProviderDetails providerDetails) throws OAuthException {
        this.providerDetails = providerDetails;
        try {
            this.providerDetails.setClientSecret(Microflows.decrypt(Core.createSystemContext(), this.providerDetails.getClientSecret()));
        }
        catch (Exception e)
        {
            throw new OAuthException("Error while decrypting provider details.", e);
        }
        validateProviderDetails();
        oauthUrl.append(providerDetails.getTenantID()).append("/oauth2/v2.0");
    }
    private AzureOAuthHandler() throws CoreException, OAuthException {
        List<IMendixObject> providers = Core.retrieveXPathQuery(Core.createSystemContext(), "//" + OAuthProviderDetails.entityName+"["+OAuthProviderDetails.MemberNames.Provider.toString()+"='"+SupportedOAuthProvider.AZURE.name()+"']");
        if(providers.isEmpty())
            throw new OAuthException("Azure OAuth provider details are not configured.");
        this.providerDetails= OAuthProviderDetails.initialize(Core.createSystemContext(), providers.get(0));
        try {
            this.providerDetails.setClientSecret(Microflows.decrypt(Core.createSystemContext(),this.providerDetails.getClientSecret()));
        }
        catch (Exception e)
        {
            throw new OAuthException("Error while decrypting provider details.", e);
        }
        oauthUrl.append(this.providerDetails.getTenantID()).append("/oauth2/v2.0");
    }
	
	AzureOAuthHandler(HttpServletResponse servletResponse, HttpServletRequest servletRequest, ISession sessionFromRequest) throws CoreException, OAuthException {
        this();
		this.servletResponse = servletResponse;
		this.servletRequest = servletRequest;
        this.sessionFromRequest = sessionFromRequest;
	}

    @Override
    public void validateProviderDetails() throws OAuthException {
        if(providerDetails==null)
            throw new OAuthException("Azure OAuth providers details cannot be empty.");
        if(providerDetails.getClientID() == null || providerDetails.getClientID().isEmpty())
            throw new OAuthException("Azure Client ID is not configured, please configure the client ID.");
        if(providerDetails.getTenantID() == null || providerDetails.getTenantID().isEmpty())
            throw new OAuthException("Azure Tenant ID is not configured, please configure the tenant ID.");
        if(providerDetails.getClientSecret() == null || providerDetails.getClientSecret().isEmpty())
            throw new OAuthException("Azure Client secret is not configured, please configure the client secret.");
    }

    @Override
	public void redirectToOAuthSignin(boolean isReAuth) throws IOException, CoreException, OAuthException {
        if(isReAuth && (servletRequest.getParameter("id") == null || servletRequest.getParameter("id").isEmpty()))
        {
            IContext context = sessionFromRequest.createContext();
            var err = new OAuthErrors(context);
            err.setErrorType("missing_inputData");
            err.setMessage("Re-authorization without EmailAccount object is not possible.");
            OAuthHelper.createAndShowError(err, Session.load(context, sessionFromRequest.getMendixObject().getId()),servletResponse);
            return;
        }
        if(isReAuth)
        {
            try
            {
                var id = Long.parseLong(servletRequest.getParameter("id"));
                IMendixObject obj = new GetEmailSettingsByID(Core.createSystemContext(), id).executeAction();
                if(obj == null)
                    throw new NullPointerException("EmailAccount not found");
            }
            catch (NumberFormatException ex) {
                IContext context = sessionFromRequest.createContext();
                var err = new OAuthErrors(context);
                err.setErrorType("invalid_inputDataType");
                err.setMessage("Re-authorization without EmailAccount object is not possible.");
                OAuthHelper.createAndShowError(err, Session.load(context, sessionFromRequest.getMendixObject().getId()),servletResponse);
                return;
            } catch (Exception e) {
                IContext context = sessionFromRequest.createContext();
                var err = new OAuthErrors(context);
                err.setErrorType("invalid_id");
                err.setMessage(e.getMessage());
                OAuthHelper.createAndShowError(err, Session.load(context, sessionFromRequest.getMendixObject().getId()),servletResponse);
                return;
            }
        }
		oauthUrl.append("/authorize")
                .append("?client_id=").append(providerDetails.getClientID())
                .append("&response_type=code")
                .append("&redirect_uri=").append(redirectUri)
                .append("&response_mode=query")
                .append("&prompt=login")
                .append("&state=").append(sessionFromRequest.getCsrfToken()).append(isReAuth ? (REAUTHACCIDSEPRATOR.concat(servletRequest.getParameter("id"))) : "")
                .append("&scope=").append(SCOPE);
		if(servletRequest.getParameter("u") != null && !servletRequest.getParameter("u").isEmpty())
		{
			oauthUrl.append("&login_hint=").append(servletRequest.getParameter("u"));
		}
                
        servletResponse.sendRedirect(oauthUrl.toString());
	}

    @Override
	public void handleCallBack() throws OAuthException, IOException, CoreException {
        if (handleErrorForAuthRequest()) return;
        if(checkCSRF()) return;
        var httpPost = prepareTokenRequest();
        String body = requestToken(httpPost);
        if (body == null) return;
        createPayloadAndRedirectToMxApp(body);
    }

    @Override
    public Token refreshExpiredToekn(Token expiredToken, IContext context) throws IOException {
        oauthUrl.append("/token");
        var httpPost = new HttpPost(oauthUrl.toString());
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("client_id", providerDetails.getClientID()));
        nvps.add(new BasicNameValuePair(REFRESH_TOKEN, expiredToken.getrefresh_token()));
        nvps.add(new BasicNameValuePair("grant_type", REFRESH_TOKEN));
        nvps.add(new BasicNameValuePair("client_secret", providerDetails.getClientSecret()));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        String body;
        try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse httpResponse = httpClient.execute(httpPost);

            HttpEntity entity = httpResponse.getEntity();
            body = EntityUtils.toString(entity);

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                Core.getLogger("Email_IMAPPOP3").error("refresh token failed " + body);
                return null;
            }
        }
        var jsonObject = new JSONObject(body);

        var t = new Token(context);
        t.setaccess_token(jsonObject.getString(ACCESS_TOKEN));
        t.settoken_type(jsonObject.getString("token_type"));
        t.setexpires_in(jsonObject.getInt("expires_in"));
        t.setscope(jsonObject.getString("scope"));
        t.setrefresh_token(jsonObject.getString(REFRESH_TOKEN));
        t.setid_token(jsonObject.getString(ID_TOKEN));
        t.getMendixObject().setValue(context, "createdDate", Date.from(Instant.now().minusSeconds(5)));
        return t;
    }

    private boolean handleErrorForAuthRequest() throws IOException, CoreException, OAuthException {
        if(servletRequest.getParameter(ERROR) != null)
        {
            IContext context = sessionFromRequest.createContext();
            var err = new OAuthErrors(context);
            err.setErrorType(servletRequest.getParameter(ERROR));
            err.setMessage(servletRequest.getParameter("error_description"));
            OAuthHelper.createAndShowError(err, Session.load(context, sessionFromRequest.getMendixObject().getId()),servletResponse);
            return true;
        }
        return false;
    }

    private boolean checkCSRF() throws IOException, CoreException, OAuthException {
        if(servletRequest.getParameter(STATE) == null || !servletRequest.getParameter(STATE).contains(sessionFromRequest.getCsrfToken()))
        {
            IContext context = sessionFromRequest.createContext();
            var err = new OAuthErrors(context);
            err.setErrorType("Possible_CSRF");
            err.setMessage("This request is not came from authentic source.");
            OAuthHelper.createAndShowError(err, Session.load(context, sessionFromRequest.getMendixObject().getId()),servletResponse);
            return true;
        }
        return false;
    }

    private void createPayloadAndRedirectToMxApp(String body) throws CoreException, IOException {
        var jsonObject = new JSONObject(body);
        IContext cntx = sessionFromRequest.createContext();
        var oAuthPayload = new OAuthPayload(cntx);
        JSONObject profileJson = OAuthHelper.getProfileFromTokenJSON(jsonObject);
        if(servletRequest.getParameter(STATE).contains(REAUTHACCIDSEPRATOR))
        {
            oAuthPayload.setreauthAccountId(servletRequest.getParameter(STATE).split("\\|ec\\|")[1]);
        }
        oAuthPayload.setemailAddress(profileJson.getString("email"));
        oAuthPayload.setpayload(Microflows.encrypt(cntx, jsonObject.toString()));
        oAuthPayload.setprovider(SupportedOAuthProvider.AZURE);
        oAuthPayload.setOAuthPayload_Session(Session.load(cntx, sessionFromRequest.getMendixObject().getId()));
        oAuthPayload.setisReAuthPayload(servletRequest.getParameter(STATE).contains(REAUTHACCIDSEPRATOR));
        oAuthPayload.commit();
        servletResponse.sendRedirect(Core.getConfiguration().getApplicationRootUrl() + "p/oauthprocess/" + oAuthPayload.getMendixObject().getId().toLong());
    }

    private String requestToken(HttpPost httpPost) throws IOException, CoreException, OAuthException {
        String body;
        try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse httpResponse = httpClient.execute(httpPost);

            HttpEntity entity = httpResponse.getEntity();
            body = EntityUtils.toString(entity);

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                IContext context = sessionFromRequest.createContext();
                var err = new OAuthErrors(context);
                var errorObj = new JSONObject(body);

                err.setHTTPStatusCode(httpResponse.getStatusLine().getStatusCode());
                err.setErrorType(errorObj.getString(ERROR));
                err.setMessage(errorObj.getString("error_description"));
                err.setErrorCodes(errorObj.getJSONArray("error_codes").toString());
                err.setTimestamp(errorObj.getString("timestamp"));
                err.setTraceID(errorObj.getString("trace_id"));
                err.setCorrelationID(errorObj.getString("correlation_id"));
                OAuthHelper.createAndShowError(err, Session.load(context, sessionFromRequest.getMendixObject().getId()),servletResponse);
                return null;
            }
        }
        return body;
    }

    private HttpPost prepareTokenRequest() throws UnsupportedEncodingException {
        oauthUrl.append("/token");
        var httpPost = new HttpPost(oauthUrl.toString());
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("client_id", providerDetails.getClientID()));
        nvps.add(new BasicNameValuePair("redirect_uri", redirectUri));
        nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));
        nvps.add(new BasicNameValuePair("client_secret", providerDetails.getClientSecret()));
        nvps.add(new BasicNameValuePair("code", servletRequest.getParameter("code")));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        return httpPost;
    }
}
