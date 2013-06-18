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

package org.zenoss.app;

import org.junit.Assert;
import org.junit.Test;
import org.zenoss.app.autobundle.FakeAppConfig;

public class AutowiredAppTest {

    private static final class TestApp extends AutowiredApp<FakeAppConfig>{

        @Override
        public String getAppName() {
            return "Test App";
        }

        @Override
        protected Class<FakeAppConfig> getConfigType() {
            return FakeAppConfig.class;
        }

    }

    @Test
    public void getTypeTest(){
        TestApp ta = new TestApp();
        Assert.assertEquals(FakeAppConfig.class, ta.getConfigType());
    }
}
