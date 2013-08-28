package org.zenoss.app.zauthbundle;

import org.apache.shiro.authc.AuthenticationToken;

public class StringAuthenticationToken implements AuthenticationToken {

    private String zenossToken;

    public StringAuthenticationToken(String zenossToken) {
        this.zenossToken = zenossToken;
    }

    @Override
    public Object getPrincipal() {
        return zenossToken;
    }

    @Override
    public Object getCredentials() {
        return zenossToken;
    }
}
