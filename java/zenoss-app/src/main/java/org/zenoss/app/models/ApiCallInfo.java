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

import com.google.common.base.*;
import java.util.Collection;

/**
 * An object that encapsulates information to be returned from a Zenoss
 * API call.  This allows Zenoss API endpoints provide callers common,
 * consistent information when errors occur.
 *
 * @param httpStatusCode   The actual http status code that is returned by the endpoint.
 * @param message          A summary of the result of the call.
 * @param details          A list of messages with further details on errors that occurred.
 * @param zenossErrorCode  A code that provides the caller more information as to what might have occurred.
 * @param zenossHelpLink   A URL that will point the caller to Zenoss documentation describing what might
 *                         have occurred and how to resolve any problems.
 */
public class ApiCallInfo {
    private final int httpStatusCode;
    private final String message;
    private final Collection<String> details;
    private final long zenossErrorCode;
    private final String zenossHelpLink;

    private ApiCallInfo(Builder builder) {
        httpStatusCode = builder.httpStatusCode;
        message = builder.message;
        details = builder.details;
        zenossErrorCode = builder.zenossErrorCode;
        zenossHelpLink = builder.zenossHelpLink;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(ApiCallInfo copy) {
        Builder builder = new Builder();
        builder.httpStatusCode = copy.httpStatusCode;
        builder.message = copy.message;
        builder.details = copy.details;
        builder.zenossErrorCode = copy.zenossErrorCode;
        builder.zenossHelpLink = copy.zenossHelpLink;
        return builder;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getMessage() {
        return message;
    }

    public Collection<String> getDetails() {
        return details;
    }

    public long getZenossErrorCode() {
        return zenossErrorCode;
    }

    public String getZenossHelpLink() {
        return zenossHelpLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ApiCallInfo that = (ApiCallInfo) o;

        return new org.apache.commons.lang3.builder.EqualsBuilder()
            .append(httpStatusCode, that.httpStatusCode)
            .append(zenossErrorCode, that.zenossErrorCode)
            .append(message, that.message)
            .append(details, that.details)
            .append(zenossHelpLink, that.zenossHelpLink)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(17, 37)
            .append(httpStatusCode)
            .append(message)
            .append(details)
            .append(zenossErrorCode)
            .append(zenossHelpLink)
            .toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("httpStatusCode", httpStatusCode)
            .add("message", message)
            .add("details", details)
            .add("zenossErrorCode", zenossErrorCode)
            .add("zenossHelpLink", zenossHelpLink)
            .toString();
    }

    public static final class Builder {
        private int httpStatusCode;
        private String message;
        private Collection<String> details;
        private long zenossErrorCode;
        private String zenossHelpLink;

        private Builder() {
        }

        public Builder withHttpStatusCode(int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withDetails(Collection<String> details) {
            this.details = details;
            return this;
        }

        public Builder withZenossErrorCode(long zenossErrorCode) {
            this.zenossErrorCode = zenossErrorCode;
            return this;
        }

        public Builder withZenossHelpLink(String zenossHelpLink) {
            this.zenossHelpLink = zenossHelpLink;
            return this;
        }

        public ApiCallInfo build() {
            return new ApiCallInfo(this);
        }
    }
}