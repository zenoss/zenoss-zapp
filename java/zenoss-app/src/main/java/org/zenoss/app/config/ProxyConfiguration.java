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
