package org.zenoss.app.zauthbundle;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.zenoss.app.config.ProxyConfiguration;

import java.io.IOException;


/**
 * This is our Shiro Realm that validates the token received from TokenFilter against the
 * ZAuth Service. This issues a separate http request to the ZAuth service and makes sure the
 * response is 200
 */
public class TokenRealm extends AuthenticatingRealm {
    private static final Logger log = LoggerFactory.getLogger(TokenRealm.class);

    /**
     * This is the url that is configured in the proxy.
     */
    private final String VALIDATE_URL = "/authorization/validate";

    /**
     * This is static so we can set it when building our bundle and it is available for all instances.
     */
    static private ProxyConfiguration proxyConfig;

    public static void setProxyConfiguration(ProxyConfiguration config) {
        proxyConfig = config;
    }

    /**
     * This tell the realm manager that we accept tokens of this type. It is required.
     */
    private Class<? extends AuthenticationToken> authenticationTokenClass = StringAuthenticationToken.class;
    public Class getAuthenticationTokenClass() {
        return authenticationTokenClass;
    }

    /**
     * The main hook for this class. It takes the token from filterToken and validates it against the
     * ZAuthService. IF that call fails or we receive a non-200 response from the service our validation fails.
     * The caller of this function expects an AuthenticationInfo class if the login is successful or an
     * AuthenticationException if the login fails
     * @param token StringAuthenticationToken instance that has our ZAuthToken
     * @return AuthenticationInfo if the login is successful
     * @throws AuthenticationException on any login failure
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        // get our principal from the token
        String zenossToken = (String)token.getPrincipal();

        // get the hostname and port from ProxyConfiguration
        String hostname = proxyConfig.getHostname();
        int port = proxyConfig.getPort();

        String url = "http://" + hostname + ":" + port + VALIDATE_URL;
        log.debug("Attempting to validate token {} against {} ", zenossToken, url);
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(url);
        method.setQueryString(new NameValuePair[]{new NameValuePair("token", zenossToken)});

        // submit a request to zauth service to find out if the token is valid
        try{
            int statusCode = client.executeMethod(method);
            String response = method.getResponseBodyAsString();
            log.debug("Response status code {} received from the zauth server. Content is {}", statusCode, response);
            if (statusCode == HttpStatus.SC_OK) {
                log.debug("Creating a new account info based on token  {}", token.getPrincipal().toString());
                AuthenticationInfo info = new SimpleAccount(zenossToken, zenossToken, "TokenRealm");
                return info;
            } else {
                log.debug("Login unsuccessful");
                throw new AuthenticationException("Unable to validate token");
            }
        } catch(IOException e) {
            log.error("IOException from ZAuthServer {} {}", e.getMessage(), e);
            throw new AuthenticationException(e);
        } finally{
            method.releaseConnection();
        }
    }
}
