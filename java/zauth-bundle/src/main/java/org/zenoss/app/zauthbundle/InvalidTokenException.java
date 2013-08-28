package org.zenoss.app.zauthbundle;

import org.apache.shiro.authc.AuthenticationException;

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
