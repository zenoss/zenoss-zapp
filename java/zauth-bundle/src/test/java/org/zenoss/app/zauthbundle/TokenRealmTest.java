package org.zenoss.app.zauthbundle;

import com.yammer.dropwizard.client.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.zenoss.app.config.ProxyConfiguration;
import org.zenoss.app.security.ZenossTenant;
import org.zenoss.app.security.ZenossToken;
import org.zenoss.app.security.ZenossUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class TokenRealmTest {


    HttpServletRequest request;
    HttpServletResponse response;
    TokenRealm realm;
    HttpClient mockClient;

    @Before
    public void setup() {
        this.request = mock(HttpServletRequest.class);
        this.response = mock(HttpServletResponse.class);
        this.mockClient = mock(HttpClient.class);
        HttpClientBuilder builder = mock(HttpClientBuilder.class);
        ClientConnectionManager connectionManager = mock(ClientConnectionManager.class);
        when(builder.build()).thenReturn(mockClient);
        when(mockClient.getConnectionManager()).thenReturn(connectionManager);
        this.realm = new TokenRealm(builder);
        TokenRealm.setProxyConfiguration(new ProxyConfiguration());
    }

    @Test
    public void testAuthenticationTokenClassIsPresent() {
        Class<? extends AuthenticationToken> cls = realm.getAuthenticationTokenClass();
        assertEquals(cls, StringAuthenticationToken.class);
    }

    @Test
    public void testGetPostMethod() throws Exception {
        HttpPost method = realm.getPostMethod("test");
        // make sure we set our token we passed in into the params
        UrlEncodedFormEntity body = (UrlEncodedFormEntity) method.getEntity();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        body.writeTo(out);
        assertEquals("id=test", out.toString(StandardCharsets.UTF_8.name()));
        assertEquals(true, method.getURI().toString().startsWith("http://"));
    }

    @Test
    public void testSuccessfulResponse() throws Exception {
        HttpResponse response = getOkResponse();
        response.addHeader(ZenossTenant.ID_HTTP_HEADER, "id");
        response.addHeader(ZenossToken.ID_HTTP_HEADER, "id");
        response.addHeader(ZenossToken.EXPIRES_HTTP_HEADER, "0");
        AuthenticationInfo info = realm.handleResponse("token", response);
        assertFalse(info.getPrincipals().isEmpty());

        ZenossToken token = info.getPrincipals().oneByType(ZenossToken.class);
        assertEquals(0.0, token.expires());
        assertEquals("id", token.id());

        ZenossTenant tenant = info.getPrincipals().oneByType(ZenossTenant.class);
        assertEquals("id", tenant.id());

        //these should be the same...
        assertEquals("token", info.getCredentials());
    }

    private HttpResponse getOkResponse() {
        StatusLine status = new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK");
        BasicHttpResponse response = new BasicHttpResponse(status);
        return response;
    }

    @Test(expected = AuthenticationException.class)
    public void testHandleBadResponse() throws Exception {
        StatusLine status = new BasicStatusLine(HttpVersion.HTTP_1_1, 401, "Unauthorized");
        HttpResponse response = new BasicHttpResponse(status);
        AuthenticationInfo info = realm.handleResponse("test", response);
    }

    @Test
    public void testDoGetAuthorization() throws Exception {
        HttpResponse response = getOkResponse();
        response.addHeader(ZenossTenant.ID_HTTP_HEADER, "id");
        response.addHeader(ZenossToken.ID_HTTP_HEADER, "id");
        response.addHeader(ZenossToken.EXPIRES_HTTP_HEADER, "0");
        when(this.mockClient.execute(any(HttpPost.class))).thenReturn(response);
        AuthenticationToken token = new StringAuthenticationToken("test");
        AuthenticationInfo results = realm.doGetAuthenticationInfo(token);
        assertEquals( "test", results.getCredentials());
    }


    @Test(expected = AuthenticationException.class)
    public void testDoGetAuthorizationMissingTenantId() throws Exception {
        HttpResponse response = getOkResponse();
        when(this.mockClient.execute(any(HttpPost.class))).thenReturn(response);
        AuthenticationToken token = new StringAuthenticationToken("test");
        realm.doGetAuthenticationInfo(token);
        fail();
    }

}
