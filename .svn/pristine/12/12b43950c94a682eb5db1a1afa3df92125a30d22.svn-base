package emailtemplate.actions;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.externalinterface.connector.RequestHandler;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.systemwideinterfaces.core.IContext;
import emailtemplate.proxies.OAuthErrors;
import system.proxies.Session;

import java.io.IOException;

public class OauthCallBackRequestHandler extends RequestHandler {

	@Override
	protected void processRequest(IMxRuntimeRequest request, IMxRuntimeResponse response, String path) throws CoreException, OAuthException, IOException {
        try {
			oauthCallback(request, response);
        } catch (Exception ex) {
			Core.getLogger("SendEmail").error("Exception occurred while processing callback request "+ex);
			IContext context = getSessionFromRequest(request).createContext();
			var err = new OAuthErrors(context);
			err.setErrorType("request_handlerError");
			err.setMessage(ex.getMessage());
			OAuthHelper.createAndShowError(err, Session.load(context, getSessionFromRequest(request).getMendixObject().getId()),response.getHttpServletResponse());
        }
	}
	
	private void oauthCallback(IMxRuntimeRequest request, IMxRuntimeResponse response) throws OAuthException, CoreException, IOException {
        var servletResponse =  response.getHttpServletResponse();
        var servletRequest =  request.getHttpServletRequest();

        
        String[] pathParameters = servletRequest.getRequestURI().split("/");
        switch (pathParameters[2]) { //NOSONAR
		case "azure":
			new AzureOAuthHandler(servletResponse , servletRequest, getSessionFromRequest(request)).handleCallBack();
			break;
		default:
			throw new OAuthException("OAuth is not yet implemented for " + pathParameters[1]);
		}
    }
}
