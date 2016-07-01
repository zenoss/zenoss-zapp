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

import org.apache.shiro.authc.AuthenticationToken;

/**
 * An implementation of AuthenticationToken that passes around our
 * ZAuthToken.
 */
public final class StringAuthenticationToken implements AuthenticationToken {

    private final String zenossToken;
    public final String extra;

    /**
     * The ZAuthToken that identifies this subject.
     * @param zenossToken String our zauthbundle token.
     */
    public StringAuthenticationToken(String zenossToken, String extra) {
        this.zenossToken = zenossToken;
        this.extra = extra;
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
