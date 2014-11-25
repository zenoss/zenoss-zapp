
package org.zenoss.dropwizardspring.testclasses;

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
