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
