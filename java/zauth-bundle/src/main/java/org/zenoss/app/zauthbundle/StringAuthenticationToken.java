package org.zenoss.app.zauthbundle;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * An implementation of AuthenticationToken that passes around our
 * ZAuthToken.
 */
public final class StringAuthenticationToken implements AuthenticationToken {

    private final String zenossToken;

    /**
     * The ZAuthToken that identifies this subject.
     * @param zenossToken String our zauthbundle token.
     */
    public StringAuthenticationToken(String zenossToken) {
        this.zenossToken = zenossToken;
    }

    /**
     * The principal and credentials are both the token in our case
     */
    @Override
    public Object getPrincipal() {
        return zenossToken;
    }

    /**
     * The principal and credentials are both the token in our case
     */
    @Override
    public Object getCredentials() {
        return zenossToken;
    }
}
