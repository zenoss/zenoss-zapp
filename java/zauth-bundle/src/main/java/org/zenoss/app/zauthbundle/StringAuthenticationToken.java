package org.zenoss.app.zauthbundle;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * An implementation of AuthenticationToken that passes around our
 * ZAuthToken.
 */
public class StringAuthenticationToken implements AuthenticationToken {

    private String zenossToken;

    /**
     * The ZAuthToken that identifies this subject.
     * @param zenossToken String our zauth token.
     */
    public StringAuthenticationToken(String zenossToken) {
        this.zenossToken = zenossToken;
    }

    @Override
    /**
     * The principal and credentials are both the token in our case
     */
    public Object getPrincipal() {
        return zenossToken;
    }

    @Override
    /**
     * The principal and credentials are both the token in our case
     */
    public Object getCredentials() {
        return zenossToken;
    }
}
