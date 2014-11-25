
package org.zenoss.app.example.api.RandomGenerator.configs;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RandomGeneratorConfig {

    @JsonProperty
    private Integer defaultMin = 0;

    @JsonProperty
    private Integer defaultMax = 10;

    public Integer getDefaultMin() {
        return defaultMin;
    }

    public Integer getDefaultMax() {
        return defaultMax;
    }

}
