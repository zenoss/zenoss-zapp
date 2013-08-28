package org.zenoss.app.zauthbundle;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


public class TokenFilter extends AuthenticatingFilter {
    private static final Logger log = LoggerFactory.getLogger(TokenFilter.class);

    private final String TOKEN_HEADER =  "X-ZAuth-Token";
    @Override
    /**
     *
     */
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String token = httpRequest.getHeader(TOKEN_HEADER);
        token = "test";
        if (token == null) {
            throw new InvalidTokenException( TOKEN_HEADER + " header is missing");
        }
        return new StringAuthenticationToken(token);
    }

    @Override
    /**
     *
     */
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        try {
            boolean result = executeLogin(request, response);
            System.out.println("Result of executeLogin " + result);
        }
        catch (InvalidTokenException e) {
            log.info("Unable to login " + e.getMessage());
        }
        catch (Exception e) {
            log.info(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e,
                                     ServletRequest request, ServletResponse response) {
        log.info(e.getMessage(), e);
        return false;
    }


}
