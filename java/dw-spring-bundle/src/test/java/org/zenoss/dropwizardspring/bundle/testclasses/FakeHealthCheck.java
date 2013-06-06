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

package org.zenoss.dropwizardspring.bundle.testclasses;

import com.yammer.metrics.core.HealthCheck;

@org.zenoss.dropwizardspring.annotations.HealthCheck
public class FakeHealthCheck extends HealthCheck {
    public FakeHealthCheck() {
        super("fake health check");
    }

    @Override
    protected Result check() throws Exception {
        return null;
    }
}
