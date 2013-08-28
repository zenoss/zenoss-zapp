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
 *
 */
public class TokenRealm extends AuthenticatingRealm {
    private static final Logger log = LoggerFactory.getLogger(TokenRealm.class);

    private final String VALIDATE_URL = "/authorization/validate";

    static private ProxyConfiguration proxyConfig;

    public static void setProxyConfiguration(ProxyConfiguration config) {
        proxyConfig = config;
    }

    private Class<? extends AuthenticationToken> authenticationTokenClass = StringAuthenticationToken.class;
    public Class getAuthenticationTokenClass() {
        return authenticationTokenClass;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        // get our principal from the token
        String zenossToken = (String)token.getPrincipal();

        // get the hostname and port from ProxyConfiguration
        String hostname = proxyConfig.getHostname();
        int port = proxyConfig.getPort();

        String url = "http://" + hostname + ":" + port + VALIDATE_URL;
        log.debug("Attempting to validate token against {}", url);
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(url);
        method.setQueryString(new NameValuePair[]{new NameValuePair("token", zenossToken)});

        // submit a request to zauth service to find out if the token is valid
        try{

            int statusCode = client.executeMethod(method);
            String newToken = method.getResponseBodyAsString();
            if (statusCode == 200) {
                // if it is create a new AuthenticationInfo object
                // get the token from the response
                System.out.println(newToken);
                log.debug("Creating a new account info based on token  {}", token.getPrincipal().toString());
                AuthenticationInfo info = new SimpleAccount(zenossToken, zenossToken, "TokenRealm");
                return info;
            } else {
                log.warn("received response {} with content {} from the ZAuth server", statusCode, newToken);
                throw new AuthenticationException("Unable to validate token");
            }
        } catch(IOException e) {
            throw new AuthenticationException(e);
        } finally{
            method.releaseConnection();
        }
    }
}
