package org.zenoss.app.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.zenoss.app.AppConfiguration;
import org.zenoss.app.example.api.RandomGenerator.configs.RandomGeneratorConfig;

import javax.validation.Valid;

public class ExampleAppConfiguration extends AppConfiguration {

    @Valid
    @JsonProperty("sub_configuration")
    private RandomGeneratorConfig randomGeneratorConfig = new RandomGeneratorConfig();

    public RandomGeneratorConfig getRandomGeneratorConfig() {
        return randomGeneratorConfig;
    }
}
