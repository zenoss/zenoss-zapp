
package org.zenoss.app.autobundle;

import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import org.zenoss.app.annotations.Bundle;

@Bundle
public class FakeBundle implements com.yammer.dropwizard.Bundle {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(Environment environment) {

    }
}
