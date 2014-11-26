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

package org.zenoss.app.config;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProxyConfiguration {

    @NotEmpty
   	@JsonProperty
   	private String protocol = "http";

	@NotEmpty
	@JsonProperty
	private String hostname = "127.0.0.1";
	
	@NotEmpty
	@JsonProperty
	private int port = 8080;

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

    public String getProtocol() {
        return protocol;
    }
}
