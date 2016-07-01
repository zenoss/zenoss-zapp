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
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

/**
 * ZenossTenant Id, identified through authentication.
 */
public class ZenossTenant {

    public static final String ID_HTTP_HEADER = "X-ZAuth-TenantId";
    private static final Interner<ZenossTenant> interner = Interners.newWeakInterner();

    public static final ZenossTenant get(String id) {
        ZenossTenant tenant = new ZenossTenant(id);
        return interner.intern(tenant);
    }

    private ZenossTenant(String id) {
        Preconditions.checkNotNull(id);
        this.id = id;
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZenossTenant that = (ZenossTenant) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ZenossTenant{" +
                "id='" + id + '\'' +
                '}';
    }

    private final String id;
}
