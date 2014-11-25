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
