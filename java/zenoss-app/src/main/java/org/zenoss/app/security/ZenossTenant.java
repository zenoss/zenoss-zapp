package org.zenoss.app.security;

import com.google.common.base.Preconditions;

/**
 * ZenossTenant Id, identified through authentication.
 */
public class ZenossTenant {

    public static final String ID_HTTP_HEADER =  "X-ZAuth-TenantId";

    public ZenossTenant( String id) {
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
