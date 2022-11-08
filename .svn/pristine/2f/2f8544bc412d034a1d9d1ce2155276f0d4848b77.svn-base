package emailtemplate.actions;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.thirdparty.org.json.JSONObject;
import emailtemplate.proxies.EmailSettings;
import emailtemplate.proxies.OAuthErrors;
import emailtemplate.proxies.OAuthProviderDetails;
import emailtemplate.proxies.Token;
import encryption.proxies.microflows.Microflows;
import system.proxies.Session;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public final class OAuthHelper 
{
    OAuthHelper(){}

	static JSONObject getProfileFromTokenJSON(JSONObject jsonObject) {
		var decoder = Base64.getUrlDecoder();
        String[] jwtToken = jsonObject.getString("id_token").split("\\.");
        var jwtBody = new String(decoder.decode(jwtToken[1]));
        return new JSONObject(jwtBody);
	}

    static void createError(OAuthErrors err, Session sessionAssociation) throws OAuthException, CoreException {
        if(sessionAssociation == null)
            throw new OAuthException("Session association cannot be null while creating oauth error.");
        err.setOAuthErrors_Session(sessionAssociation);
        err.commit();
    }

    static void createAndShowError(OAuthErrors err, Session sessionAssociation, HttpServletResponse servletResponse) throws IOException, CoreException, OAuthException {
        createError(err, sessionAssociation);
        servletResponse.sendRedirect(Core.getConfiguration().getApplicationRootUrl() + "p/oautherror/" + err.getMendixObject().getId().toLong());
    }

    static Token getTokenByEmailAccount(EmailSettings emailAccount, IContext context) throws OAuthException {
        Token token = null;
        List<IMendixObject> tokens = Core.retrieveByPath(context, emailAccount.getMendixObject(), Token.MemberNames.Token_EmailSettings.toString());
        for (IMendixObject __token : tokens)
            token = emailtemplate.proxies.Token.initialize(context, __token);
        if(token == null)
            throw new OAuthException("OAuth token not found for the provided email account");
        try {
            token.setaccess_token(Microflows.decrypt(Core.createSystemContext(),token.getaccess_token()));
            token.setrefresh_token(Microflows.decrypt(Core.createSystemContext(),token.getrefresh_token()));
        }
        catch (Exception e)
        {
            throw new OAuthException("Error while decrypting token information.");
        }
        return token;
    }

    static OAuthProviderDetails getOAuthProviderDetailsByEmailAccount(EmailSettings emailAccount, IContext context) throws OAuthException {
        OAuthProviderDetails providerDetails = null;
        List<IMendixObject> oAuthProviderDetails = Core.retrieveByPath(context, emailAccount.getMendixObject(), EmailSettings.MemberNames.EmailSettings_OAuthProviderDetails.toString());
        for (IMendixObject provider : oAuthProviderDetails)
            providerDetails = OAuthProviderDetails.initialize(context, provider);
        if(providerDetails == null)
            throw new OAuthException("OAuth provider details not found for the provided email account");
        return providerDetails;
    }

    static void fetchOrRefreshToken(EmailSettings account, IContext context) throws OAuthException, IOException, CoreException {
        var token = getTokenByEmailAccount(account, context);
        var providerDetails = getOAuthProviderDetailsByEmailAccount(account, context);
        Date created = token.getMendixObject().getValue(context, "createdDate");
        var instant = created.toInstant().plusSeconds(token.getexpires_in());
        if(instant.getEpochSecond() < Instant.now().getEpochSecond()) {
            Token newToken;
            Core.getLogger("SendEmail").debug("OAuth Token expired for "+ account.getUserName());
            switch (providerDetails.getProvider()) //NOSONAR
            {
                case AZURE:
                    newToken = new AzureOAuthHandler(providerDetails).refreshExpiredToekn(token, context);
                    break;

                default:
                    throw new OAuthException("Refresh token is not yet implemented for provider" + providerDetails.getProvider());
            }
            if(newToken == null)
                throw new OAuthException("Unable to refresh the token, try to re-authorize the application again by visiting 'Configuration` page.");
            account.setPassword(newToken.getaccess_token());
            try
            {

                newToken.setrefresh_token(Microflows.encrypt(context, newToken.getrefresh_token()));
                newToken.setaccess_token(Microflows.encrypt(context, newToken.getaccess_token()));
                newToken.setid_token(Microflows.encrypt(context, newToken.getid_token()));
            }
            catch (Exception ex)
            {
                throw new OAuthException("Error while encrypting token information.");
            }
            Core.delete(context, token.getMendixObject());
            newToken.setToken_EmailSettings(account);
            Core.commit(context, newToken.getMendixObject());
            context.endTransaction();
            Core.getLogger("SendEmail").debug("OAuth Token re-newed for "+ account.getUserName());
        }
        else {
            try {
                account.setPassword(Microflows.decrypt(Core.createSystemContext(),token.getaccess_token()));
            }
            catch (Exception ex) {
                throw new OAuthException("Error while decrypting token information.");
            }
        }
    }
}
