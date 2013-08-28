package org.zenoss.app.zauthbundle;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zenoss.app.AppConfiguration;
import org.zenoss.app.config.ProxyConfiguration;

import java.io.IOException;


/**
 *
 */
@Component
public class TokenRealm extends AuthenticatingRealm {
    private static final Logger log = LoggerFactory.getLogger(TokenRealm.class);

    private final String VALIDATE_URL = "/zauth/api/validate";

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
        log.debug("Attempting to validate token against hostname:port {}, {}", hostname, port);
        String url = "http://" + hostname + ":" + port + VALIDATE_URL;
        HttpClient client = new HttpClient();
        client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        client.setTimeout(5);
        ContentExchange exchange = new ContentExchange(true);
        exchange.setURL(url + "?token=" + zenossToken);
        System.out.println(url + "?token=" + zenossToken);
        // submit a request to zauth service to find out if the token is valid
        try{
            client.send(exchange);
            int exchangeState = exchange.waitForDone();
            if (exchange.getResponseStatus() == 200) {
                // if it is create a new AuthenticationInfo object
                // get the token from the response
                String newToken = exchange.getResponseContent();
                log.debug("Creating a new account info based on token  {}", token.getPrincipal().toString());
                AuthenticationInfo info = new SimpleAccount(newToken, newToken, "TokenRealm");
                return info;
            } else {
                log.warn("received response {} with content {} from the ZAuth server", exchange.getResponseStatus(), exchange.getResponseContent());
                throw new AuthenticationException("Unable to validate token");
            }
        } catch(IOException e) {
            throw new AuthenticationException(e);
        } catch(InterruptedException e) {
            throw new AuthenticationException(e);
        }
    }
}
