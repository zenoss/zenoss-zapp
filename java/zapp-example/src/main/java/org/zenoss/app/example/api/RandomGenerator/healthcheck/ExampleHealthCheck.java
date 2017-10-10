package org.zenoss.app.example.api.RandomGenerator.healthcheck;


import org.zenoss.dropwizardspring.annotations.HealthCheck;

@HealthCheck
public class ExampleHealthCheck extends com.codahale.metrics.health.HealthCheck{
    @Override
    protected Result check() throws Exception {
        return Result.healthy("All good");
    }
}
