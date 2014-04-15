package org.zenoss.app.zauthbundle;

import com.google.common.base.Preconditions;
import com.yammer.dropwizard.client.HttpClientBuilder;
import com.yammer.dropwizard.client.HttpClientConfiguration;
import com.yammer.dropwizard.util.Duration;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenoss.app.config.ProxyConfiguration;
import org.zenoss.app.security.ZenossTenant;
import org.zenoss.app.security.ZenossToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * This is our Shiro Realm that validates the token received from TokenFilter against the
 * ZAuth Service. This issues a separate http request to the ZAuth service and makes sure the
 * response is 200
 */
public class TokenRealm extends AuthenticatingRealm {

    private static final Logger log = LoggerFactory.getLogger(TokenRealm.class);
    private static Class<StringAuthenticationToken> authenticationTokenClass = StringAuthenticationToken.class;
    private static final String VALIDATE_URL = "/zauth/api/validate";

    /**
     * This is static so we can set it when building our bundle and it is available for all instances.
     */
    private static ProxyConfiguration proxyConfig;

    static void setProxyConfiguration(ProxyConfiguration config) {
        proxyConfig = config;
    }

    private static String getValidateUrl() {
        Preconditions.checkState(proxyConfig != null, "Proxy configuration not set");
        // get the hostname and port from ProxyConfiguration
        final String hostname = proxyConfig.getHostname();
        final int port = proxyConfig.getPort();
        return "http://" + hostname + ":" + port + VALIDATE_URL;
    }

    private final HttpClientBuilder httpClientBuilder;

    // Default constructor for Shiro reflection
    public TokenRealm() {
        // Let's hope the defaults in dropwizard are sensible.
        HttpClientConfiguration config = new HttpClientConfiguration();
        config.setConnectionTimeout(Duration.seconds(10));
        config.setTimeout(Duration.seconds(10));
        this.httpClientBuilder = new HttpClientBuilder().using(config);
    }

    // Constructor when HttpClient configuration is desired.
    public TokenRealm(HttpClientBuilder httpClientBuilder) {
        this.httpClientBuilder = httpClientBuilder;
    }

    /**
     * This tell the realm manager that we accept tokens of this type. It is required.
     */
    @Override
    public Class<StringAuthenticationToken> getAuthenticationTokenClass() {
        return authenticationTokenClass;
    }

    /**
     * The main hook for this class. It takes the token from filterToken and validates it against the
     * ZAuthService. IF that call fails or we receive a non-200 response from the service our validation fails.
     * The caller of this function expects an AuthenticationInfo class if the login is successful or an
     * AuthenticationException if the login fails
     *
     * @param token StringAuthenticationToken instance that has our ZAuthToken
     * @return AuthenticationInfo if the login is successful
     * @throws AuthenticationException on any login failure
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        log.debug("authenticating token - aka token validation");
        // get our principal from the token
        String zenossToken = (String) token.getPrincipal();

        // submit a request to zauthbundle service to find out if the token is valid
        HttpClient client = httpClientBuilder.build();
        try {
            HttpPost method = getPostMethod(zenossToken);
            HttpResponse response = client.execute(method);

            return handleResponse(zenossToken, response);
        } catch (IOException e) {
            log.error("IOException from ZAuthServer {} {}", e.getMessage(), e);
            throw new AuthenticationException(e);
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            client.getConnectionManager().shutdown();
        }
    }

    /**
     * Interpret the http response from the post to the zauth service
     *
     * @param response HttpStatus code from our resposne
     * @return AuthenticationToken or an AuthenticationException
     * @throws IOException             if there was an error validating the token
     * @throws AuthenticationException if the token wasn't valid
     */
    AuthenticationInfo handleResponse(String token, HttpResponse response) throws IOException, AuthenticationException {
        int statusCode = response.getStatusLine().getStatusCode();
        // If debug is enabled, log the response body
        if (log.isDebugEnabled()) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                response.getEntity().writeTo(os);
                String responseBody = os.toString(StandardCharsets.UTF_8.name());

                //avoid printing token id
                if (statusCode >= 200 && statusCode < 300) {
                    responseBody = "Success!";
                }

                log.debug("Response status code {} received from the zauthbundle server. Content is {}",
                        statusCode, responseBody);
            }
        }

        // Response of 200 means validation succeeded
        if (statusCode == HttpStatus.SC_OK) {
            String tokenId = getHeaderValue(ZenossToken.ID_HTTP_HEADER, response);
            String tokenExp = getHeaderValue(ZenossToken.EXPIRES_HTTP_HEADER, response);
            String tenantId = getHeaderValue(ZenossTenant.ID_HTTP_HEADER, response);
            log.debug("Validated request: token='********', token expires={}, tenant={}", tokenExp, tenantId);

            ZenossAuthenticationInfo info = new ZenossAuthenticationInfo(token, TokenRealm.class.toString());
            info.addTenant(tenantId, TokenRealm.class.toString());
            info.addToken(tokenId, Double.valueOf(tokenExp), TokenRealm.class.toString());

            return info;
        } else {
            log.debug("Login unsuccessful");
            throw new AuthenticationException("Unable to validate token");
        }
    }

    /**
     * Creates a new Post method based on the proxy config and the passed in token that we
     * want to verify.
     *
     * @param zenossToken token received from the ZAuth header
     * @return PostMethod for our http client
     */
    HttpPost getPostMethod(String zenossToken) throws UnsupportedEncodingException {
        String url = getValidateUrl();
        log.debug("Attempting to validate token {} against {} ", zenossToken, url);
        HttpPost method = new HttpPost(url);
        List<BasicNameValuePair> params = Collections.singletonList(new BasicNameValuePair("id", zenossToken));
        method.setEntity(new UrlEncodedFormEntity(params));

        return method;
    }

    String getHeaderValue(String header, HttpResponse response) {
        Header[] headers = response.getHeaders(header);
        if (headers == null || headers.length <= 0) {
            throw new AuthenticationException("Missing response header: " + header);
        }
        return headers[0].getValue();
    }
}
