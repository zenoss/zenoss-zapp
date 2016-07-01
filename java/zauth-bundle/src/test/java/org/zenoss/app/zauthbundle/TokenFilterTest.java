// Copyright 2014 The Serviced Authors.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.zenoss.app.zauthbundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class TokenFilterTest {

    Subject subject;
    HttpServletRequest request;
    HttpServletResponse response;
    TokenFilter filt;

    @Before
    public void setup() {
        this.subject = mock(Subject.class);
        this.request = mock(HttpServletRequest.class);
        this.response = mock(HttpServletResponse.class);
        this.filt = new TokenFilter();
    }

    @After
    public void after() {
        this.request = null;
        this.response = null;
        this.filt = null;
    }

    @Test(expected = InvalidTokenException.class)
    public void testMissingTokenHeader() throws Exception{
        filt.createToken(request, response);
    }

    @Test
    public void testTokenHeaderPresent() throws Exception{
        when(request.getHeader(TokenFilter.TOKEN_HEADER)).thenReturn("test");
        AuthenticationToken t = filt.createToken(request, response);
        assertEquals(t.getPrincipal(), "test");
        assertEquals(t.getCredentials(), "test");
    }

    @Test()
    public void testMissingHeaderInAccessDenied() throws Exception{
        boolean result = filt.onAccessDenied(request, response);
        assertFalse(result);
    }

    @Test
    public void testOnLoginFailure() throws Exception{
        AuthenticationToken token = new StringAuthenticationToken("test", "");
        AuthenticationException e = new AuthenticationException("This is an authentication exception");

        boolean result = filt.onLoginFailure(token, e, request, response);
        assertEquals(false, result);
        verify(response, times(1)).setStatus(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void testOnLoginSuccessReturnsTrue() throws Exception {
        Object principle = new Object();
        when(subject.getPrincipal()).thenReturn(principle);

        boolean result = filt.onLoginSuccess(null, subject, request, response);
        assertTrue(result);
    }

}
