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

package org.zenoss.app.security;

import com.google.common.base.Preconditions;

/**
 * ZenossToken manages data associated within a zenoss realm. The data's
 * captured through each successful token validation.
 */
public class ZenossToken {

    public static final String ID_HTTP_HEADER = "X-ZAuth-TokenId";
    public static final String EXPIRES_HTTP_HEADER = "X-ZAuth-TokenExpiration";

    public ZenossToken(String id, double expires) {
        Preconditions.checkNotNull(id);
        this.id = id;
        this.expires = expires;
    }

    public String id() {
        return id;
    }

    public double expires() {
        return expires;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZenossToken that = (ZenossToken) o;

        if (expires != that.expires) return false;
        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id.hashCode();
        temp = Double.doubleToLongBits(expires);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ZenossToken{" +
                "id='********'" +
                ", expires=" + expires +
                '}';
    }

    private final String id;
    private final double expires;
}
