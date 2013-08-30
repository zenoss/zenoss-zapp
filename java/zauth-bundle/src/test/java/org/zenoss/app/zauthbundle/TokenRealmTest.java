package org.zenoss.app.zauthbundle;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.junit.Before;
import org.junit.Test;
import org.zenoss.app.config.ProxyConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
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
        this.realm = new TokenRealm();
        TokenRealm.setProxyConfiguration(new ProxyConfiguration());
        this.mockClient = mock(HttpClient.class);
        realm.setHttpClient(this.mockClient);
    }

    @Test
    public void testAuthenticationTokenClassIsPresent() {
        Class<? extends AuthenticationToken> cls = realm.getAuthenticationTokenClass();
        assertEquals(cls, StringAuthenticationToken.class);
    }

    @Test
    public void testgetPostMethod() throws Exception{
        PostMethod method = realm.getPostMethod("test");
        // make sure we set our token we passed in into the query string
        assertEquals("id=test", method.getQueryString());
        assertEquals(true, method.getURI().toString().startsWith("http://"));
    }

    @Test
    public void testSuccessfulResponse() throws Exception {
        AuthenticationInfo info = realm.handleResponse("test", 200, realm.getPostMethod("test"));
        assertFalse(info.getPrincipals().isEmpty());
        assertEquals(info.getCredentials().toString(), "test");
    }

    @Test(expected = AuthenticationException.class)
    public void testHandleBadResponse() throws Exception{
        AuthenticationInfo info = realm.handleResponse("test", 401, realm.getPostMethod("test"));
    }

    @Test
    public void testDoGetAuthorization() throws Exception {
        when(this.mockClient.executeMethod(any(PostMethod.class))).thenReturn(200);
        AuthenticationToken token = new StringAuthenticationToken("test");
        AuthenticationInfo results = realm.doGetAuthenticationInfo(token);
        assertEquals(results.getCredentials().toString(), "test");
    }

}
