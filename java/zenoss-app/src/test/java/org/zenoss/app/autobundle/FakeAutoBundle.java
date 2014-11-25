
package org.zenoss.app.autobundle;

import com.google.common.base.Optional;
import com.yammer.dropwizard.Bundle;

public class FakeAutoBundle implements AutoBundle {
    @Override
    public Bundle getBundle() {
        return new FakeBundle();
    }

    @Override
    public Optional<Class> getRequiredConfig() {
        return Optional.of((Class)FakeConfig.class);
    }

}
