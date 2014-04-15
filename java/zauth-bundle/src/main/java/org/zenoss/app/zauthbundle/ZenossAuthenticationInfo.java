package org.zenoss.app.zauthbundle;

import com.google.common.base.Preconditions;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.zenoss.app.security.ZenossTenant;
import org.zenoss.app.security.ZenossToken;

/**
 * Shiro authentication information.  Supports realm based token and tenant shiro principles.
 */
public class ZenossAuthenticationInfo implements AuthenticationInfo {

    public ZenossAuthenticationInfo(String token, String realm) {
        Preconditions.checkNotNull(token);
        this.token = token;
        this.principles.add( token, realm);
    }

    public void addToken( String id, double expires, String realm) {
        Object principle = new ZenossToken(id, expires);
        this.principles.add( principle, realm);
    }

    public void addTenant( String id, String realm) {
        Object principle = new ZenossTenant(id);
        this.principles.add( principle, realm);
    }

    @Override
    public PrincipalCollection getPrincipals() {
        return principles;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    private final String token;
    private final SimplePrincipalCollection principles = new SimplePrincipalCollection();
}
