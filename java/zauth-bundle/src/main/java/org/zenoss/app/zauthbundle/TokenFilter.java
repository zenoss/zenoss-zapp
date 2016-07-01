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

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class intercepts all non-static http requests and extracts the ZAuth Token from the
 * Http headers. This header must be present on all zapp requests for authentication.
 *
 * This class creates the AuthenticationToken that our Realm then validates against the ZAuthService.
 */
public class TokenFilter extends AuthenticatingFilter {
    private static final Logger log = LoggerFactory.getLogger(TokenFilter.class);

    // Package private for testing
    static final String TOKEN_HEADER =  "X-ZAuth-Token";

    /**
     * Create our token from the ZAuth Header.
     * @param request The http request.
     * @param response Unused, this is the http response.
     * @return StringAuthenticationToken representing the subject.
     * @throws InvalidTokenException if the header is missing.
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws InvalidTokenException {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String token = httpRequest.getHeader(TOKEN_HEADER);
        log.debug("Created login token");
        if (token == null) {
            throw new InvalidTokenException(TOKEN_HEADER + " header is missing");
        }
        String extra = httpRequest.getHeader(HttpHeaders.USER_AGENT);
        return new StringAuthenticationToken(token, extra);
    }

    /**
     * This is the main hook into this class. It is called when a user attempts to access a resource
     * and is not authenticated. We first check to see if we can login (executeLogin as defined on the parent class)
     *
     * @param request The http request the client initiated.
     * @param response Http response, a 401 status code is set if the token is missing and a 500 is sent if there is an error
     * @return boolean true if the request can continue or false if it should be stopped (unauthorized or an error)
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        try {
            return executeLogin(request, response);
        }
        catch (InvalidTokenException e) {
            log.error("Unable to login: " + e.getMessage());
            // let the client know we are unauthorized
            WebUtils.toHttp(response).setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }
        /**
         * Catch all exception so that we don't miss any logic errors from our Realm impl.
         */
        catch (Exception e) {
            // Record the exception
            log.error(e.getMessage(), e);
            // Let Shiro handle this.
            // It will do some stuff, then rethrow for the servlet container.
            throw e;
        }
    }


    /**
     * This is executed when the login fails.
     * @param token our authentication token from createToken
     * @param e if there was an exception that caused this
     * @param request HttpRequest
     * @param response HttpResponse, a 401 status is set.
     * @return boolean false if the login fails we should not continue the filter chain
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e,
                                     ServletRequest request, ServletResponse response) {
        if (e != null){
            // not sure if we should log on general failures.
            log.debug(e.getMessage(), e);
        }
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        // let the client know we are unauthorized if we haven't set an error status already
        if (httpResponse.getStatus() != HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            httpResponse.setStatus(HttpStatus.SC_UNAUTHORIZED);
        }
        return false;
    }


    /**
     * Explicitly tell the filter chain that we wish to continue processing when a
     * user logs in.
     * @param token our AuthenticationToken
     * @param subject the logged in user
     * @param request ServletRequest
     * @param response Http response back to the user
     * @return true, let the filter chain continue on
     * @throws Exception
     */
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject,
                                     ServletRequest request, ServletResponse response) throws Exception {
        // We logged in, let the original request continue on.
        log.debug( "onLoginSuccess(): setting servlet-request subject");
        request.setAttribute( "zenoss-subject", subject);
        return true;
    }
}
