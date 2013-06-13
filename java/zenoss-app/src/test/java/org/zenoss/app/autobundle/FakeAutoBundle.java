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
