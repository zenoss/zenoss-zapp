package org.zenoss.app.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class CorsConfiguration {

    @NotEmpty
   	@JsonProperty
   	private String methods = "GET,PUT,POST,DELETE,OPTIONS";

    @NotEmpty
   	@JsonProperty
   	private String origins = "*";

    @NotEmpty
   	@JsonProperty
   	private String headers = "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin";

    @NotEmpty
   	@JsonProperty
   	private String urlMapping = "/*";

    public String getMethods() {
        return methods;
    }

    public String getOrigins() {
        return origins;
    }

    public String getHeaders() {
        return headers;
    }

    public String getUrlMapping() {
        return urlMapping;
    }
}
