/*
 * ****************************************************************************
 *
 *  Copyright (C) Zenoss, Inc. 2013, all rights reserved.
 *
 *  This content is made available according to terms specified in
 *  License.zenoss under the directory where your Zenoss product is installed.
 *
 * ***************************************************************************
 */

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
