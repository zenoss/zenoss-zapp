package org.zenoss.app.zauthbundle;

import org.apache.shiro.authc.AuthenticationException;

/**
 * This exception is raised if we do not receive a ZAuth token in our http request.
 */
public class InvalidTokenException extends AuthenticationException {

    /**
     * Constructs a new InvalidTokenException
     *
     * @param message the reason for the exception
     */
    public InvalidTokenException(String message) {
        super(message);
    }

}
