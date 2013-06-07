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
