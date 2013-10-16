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

import com.fasterxml.jackson.databind.SerializationFeature;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import org.junit.Assert;
import org.junit.Test;
import org.zenoss.app.autobundle.FakeAppConfig;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        @Override
        protected String[] getActivateProfiles() {
            return new String[]{"test"};
        }

    }

    @Test
    public void getTypeTest(){
        TestApp ta = new TestApp();
        Assert.assertEquals(FakeAppConfig.class, ta.getConfigType());
    }

    @Test
    public void testGetActivateProfiles(){
        TestApp ta = new TestApp();
        Assert.assertArrayEquals(new String[]{"test"}, ta.getActivateProfiles());

        AutowiredApp test = new AutowiredApp<AppConfiguration>(){
            @Override
            public String getAppName() {
                return "test";
            }

            @Override
            protected Class<AppConfiguration> getConfigType() {
                return AppConfiguration.class;
            }

        };

        Assert.assertArrayEquals(AutowiredApp.DEFAULT_ACTIVE_PROFILES, test.getActivateProfiles());


    }

    @Test
    public void testRun() throws Exception {
        final Environment environment = mock(Environment.class);

        ObjectMapperFactory omf = mock(ObjectMapperFactory.class);
        when(environment.getObjectMapperFactory()).thenReturn(omf);
        TestApp ta = new TestApp();
        ta.run(new FakeAppConfig(), environment);
        verify(omf).enable(SerializationFeature.INDENT_OUTPUT);

    }

    @Test
    public void testInitialize() throws Exception {
        final Environment environment = mock(Environment.class);
        Bootstrap bootstrap = mock(Bootstrap.class);

        ObjectMapperFactory omf = mock(ObjectMapperFactory.class);
        when(environment.getObjectMapperFactory()).thenReturn(omf);
        TestApp ta = new TestApp();
        ta.initialize(bootstrap);

    }

}
