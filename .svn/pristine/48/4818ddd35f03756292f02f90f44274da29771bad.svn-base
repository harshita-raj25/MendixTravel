package emailtemplate.actions;

import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import emailtemplate.proxies.Token;

import java.io.IOException;

public interface OAuthHandler {
    void validateProviderDetails() throws OAuthException;
    void redirectToOAuthSignin(boolean isReAuth) throws IOException, CoreException, OAuthException;
    void handleCallBack() throws OAuthException, IOException, CoreException;
    Token refreshExpiredToekn(Token expiredToken, IContext context) throws IOException;
}
