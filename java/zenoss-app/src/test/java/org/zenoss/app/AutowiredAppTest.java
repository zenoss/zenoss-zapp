// Copyright 2014 The Serviced Authors.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package org.zenoss.app;

import be.tomcools.dropwizard.websocket.WebsocketBundle;
import io.dropwizard.Application;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.zenoss.app.autobundle.FakeAppConfig;
import org.zenoss.app.testclasses.TestWebSocket;

import javax.websocket.server.ServerEndpointConfig;

import static org.mockito.Mockito.*;

public class AutowiredAppTest {

    public static final class TestApp extends AutowiredApp<FakeAppConfig> {

        WebsocketBundle websocketBundle = mock(WebsocketBundle.class);

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

        @Override
        WebsocketBundle getWebsocket() {
            return websocketBundle;
        }
    }


    @Test
    public void getTypeTest() {
        TestApp ta = new TestApp();
        Assert.assertEquals(FakeAppConfig.class, ta.getConfigType());
    }

    @Test
    public void testGetActivateProfiles() {
        TestApp ta = new TestApp();
        Assert.assertArrayEquals(new String[]{"test"}, ta.getActivateProfiles());

        AutowiredApp test = new AutowiredApp<AppConfiguration>() {
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
    public void testInit() throws Exception {
        final TestApp testApp = new TestApp();
        final DropwizardTestSupport app =
                new DropwizardTestSupport(TestApp.class, null, new ConfigOverride[]{}) {
                    @Override
                    public Application newApplication() {
                        return testApp;
                    }
                };
        app.before();
        app.after();

        verify(testApp.websocketBundle, times(1)).addEndpoint(isA(ServerEndpointConfig.class));
        verify(testApp.websocketBundle, times(1)).addEndpoint(TestWebSocket.class);

    }


}
