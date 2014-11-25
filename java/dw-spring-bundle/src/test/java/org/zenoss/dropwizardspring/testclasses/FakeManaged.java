
package org.zenoss.dropwizardspring.testclasses;

import com.yammer.dropwizard.lifecycle.Managed;

@org.zenoss.dropwizardspring.annotations.Managed
public class FakeManaged implements Managed {

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }
}
