package org.zenoss.app.zauthbundle;

import com.google.common.base.Preconditions;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.zenoss.app.security.ZenossTenant;
import org.zenoss.app.security.ZenossToken;
import org.zenoss.app.security.ZenossUser;

/**
 *
 * Shiro authentication information
 *
 * Created with IntelliJ IDEA.
 * User: scleveland
 * Date: 4/3/14
 * Time: 11:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class ZenossAuthenticationInfo implements AuthenticationInfo {

    public ZenossAuthenticationInfo(String token, String realm) {
        Preconditions.checkNotNull(token);
        this.token = token;
        this.principles.add( token, realm);
    }

    public void addUser( String username, String password, String realm) {
        Object principle = new ZenossUser( username, password);
        this.principles.add( principle, realm);
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
