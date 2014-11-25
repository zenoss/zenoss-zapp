
package org.zenoss.app.autobundle;

import org.zenoss.app.AppConfiguration;

public class FakeAppConfig extends AppConfiguration implements FakeConfig {
    @Override
    public String getBlam() {
        return null;
    }
}
