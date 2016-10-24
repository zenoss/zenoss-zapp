// Copyright 2014-2016 The Serviced Authors.
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

package org.zenoss.app.models;

import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * An object that encapsulates information to be returned from a Zenoss
 * API call.  This allows Zenoss API endpoints provide callers common,
 * consistent information when errors occur.
 *
 * @param httpStatusCode   The actual http status code that is returned by the endpoint.
 * @param errorString      A string describing the problem that ocurred.
 * @param zenossStatusCode A code, possibly different from the http code, that provides the
 *                         caller more information as to what might have ocurred.
 * @param zenossHelpLink   A URL that will point the caller to documentation describing what might
 *                         have ocurred and how to resolve any problems.
 */
public class ApiCallInfo {
    private final int httpStatusCode;
    private final String errorString;
    private final long zenossStatusCode;
    private final String zenossHelpLink;

    private ApiCallInfo(Builder builder) {
        httpStatusCode = builder.httpStatusCode;
        errorString = builder.errorString;
        zenossStatusCode = builder.zenossStatusCode;
        zenossHelpLink = builder.zenossHelpLink;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(org.zenoss.app.models.ApiCallInfo copy) {
        Builder builder = new Builder();
        builder.httpStatusCode = copy.httpStatusCode;
        builder.errorString = copy.errorString;
        builder.zenossStatusCode = copy.zenossStatusCode;
        builder.zenossHelpLink = copy.zenossHelpLink;
        return builder;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getErrorString() {
        return errorString;
    }

    public long getZenossStatusCode() {
        return zenossStatusCode;
    }

    public String getZenossHelpLink() {
        return zenossHelpLink;
    }

    public boolean equals(Object object) {
        if (this == object) return true;

        if (object == null || getClass() != object.getClass()) return false;

        ApiCallInfo that = (ApiCallInfo) object;
        return new EqualsBuilder()
            .appendSuper(super.equals(object))
            .append(httpStatusCode, that.httpStatusCode)
            .append(zenossStatusCode, that.zenossStatusCode)
            .append(errorString, that.errorString)
            .append(zenossHelpLink, that.zenossHelpLink)
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .appendSuper(super.hashCode())
            .append(httpStatusCode)
            .append(errorString)
            .append(zenossStatusCode)
            .append(zenossHelpLink)
            .toHashCode();
    }

    @java.lang.Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("httpStatusCode", httpStatusCode)
            .add("errorString", errorString)
            .add("zenossStatusCode", zenossStatusCode)
            .add("zenossHelpLink", zenossHelpLink)
            .toString();
    }

    public static final class Builder {
        private int httpStatusCode;
        private String errorString;
        private long zenossStatusCode;
        private String zenossHelpLink;

        private Builder() {
        }

        public Builder withHttpStatusCode(int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public Builder withErrorString(String errorString) {
            this.errorString = errorString;
            return this;
        }

        public Builder withZenossStatusCode(long zenossStatusCode) {
            this.zenossStatusCode = zenossStatusCode;
            return this;
        }

        public Builder withZenossHelpLink(String zenossHelpLink) {
            this.zenossHelpLink = zenossHelpLink;
            return this;
        }

        public org.zenoss.app.models.ApiCallInfo build() {
            return new ApiCallInfo(this);
        }
    }
}