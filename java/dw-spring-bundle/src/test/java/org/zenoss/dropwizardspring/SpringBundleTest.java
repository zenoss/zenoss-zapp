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


package org.zenoss.dropwizardspring;

public class SpringBundleTest {

//    class TestConfiguration extends Configuration implements SpringConfiguration {
//        public WebSocketConfiguration getWebSocketConfiguration() {
//            return new WebSocketConfiguration();
//        }
//
//        public EventBusConfiguration getEventBusConfiguration() {
//            return new EventBusConfiguration();
//        }
//    }
//
//    @Test
//    public void scanTest() throws Exception {
//        SpringBundle sb = new SpringBundle("org.zenoss.dropwizardspring.testclasses");
//        Configuration config = new TestConfiguration();
//        Environment environment = mock(Environment.class);
//        Bootstrap bootStrap = mock(Bootstrap.class);
//        ExecutorService service = mock(ExecutorService.class);
//        when(environment.managedExecutorService(anyString(), anyInt(), anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(service);
//        sb.initialize(bootStrap);
//        sb.run(config, environment);
//        verify(environment, times(1)).addResource(sb.applicationContext.getBean("fakeResource"));
//        verify(environment, times(1)).addHealthCheck(sb.applicationContext.getBean("fakeHealthCheck", HealthCheck.class));
//        verify(environment, times(1)).addTask(sb.applicationContext.getBean("fakeTask", Task.class));
//        verify(environment, times(1)).manage(sb.applicationContext.getBean("fakeManaged", Managed.class));
//    }
//
//    @Test
//    public void testSetDevProfile() throws Exception {
//        SpringBundle sb = new SpringBundle("org.zenoss.dropwizardspring.testclasses");
//        sb.setDefaultProfiles("dev");
//        Configuration config = new TestConfiguration();
//        Environment environment = mock(Environment.class);
//        ExecutorService service = mock(ExecutorService.class);
//        when(environment.managedExecutorService(anyString(), anyInt(), anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(service);
//        sb.run(config, environment);
//        Assert.assertTrue(sb.applicationContext.containsBean("devProfile"));
//        Assert.assertFalse(sb.applicationContext.containsBean("testProfile"));
//    }
//
//
//    @Test
//    public void testSetTestProfiles() throws Exception {
//        SpringBundle sb = new SpringBundle("org.zenoss.dropwizardspring.testclasses");
//        sb.setDefaultProfiles("dev");
//        Configuration config = new TestConfiguration();
//        Environment environment = mock(Environment.class);
//        ExecutorService service = mock(ExecutorService.class);
//        when(environment.managedExecutorService(anyString(), anyInt(), anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(service);
//        sb.run(config, environment);
//        Assert.assertTrue(sb.applicationContext.containsBean("devProfile"));
//        Assert.assertFalse(sb.applicationContext.containsBean("testProfile"));
//    }
//
//    @Test
//    public void testSetTwoProfiles() throws Exception {
//        SpringBundle sb = new SpringBundle("org.zenoss.dropwizardspring.testclasses");
//        sb.setDefaultProfiles("dev", "test");
//        Configuration config = new TestConfiguration();
//        Environment environment = mock(Environment.class);
//        ExecutorService service = mock(ExecutorService.class);
//        when(environment.managedExecutorService(anyString(), anyInt(), anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(service);
//        sb.run(config, environment);
//        Assert.assertTrue(sb.applicationContext.containsBean("devProfile"));
//        Assert.assertTrue(sb.applicationContext.containsBean("testProfile"));
//    }
//
//    @Test
//    public void testSetNoProfiles() throws Exception {
//        SpringBundle sb = new SpringBundle("org.zenoss.dropwizardspring.testclasses");
//        sb.setDefaultProfiles(new String[]{});
//        Configuration config = new TestConfiguration();
//        Environment environment = mock(Environment.class);
//        ExecutorService service = mock(ExecutorService.class);
//        when(environment.managedExecutorService(anyString(), anyInt(), anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(service);
//        sb.run(config, environment);
//        Assert.assertFalse(sb.applicationContext.containsBean("devProfile"));
//        Assert.assertFalse(sb.applicationContext.containsBean("testProfile"));
//    }
//
//    @Test(expected = IllegalStateException.class)
//    public void testNoPathWebSocket() throws Exception {
//        SpringBundle sb = new SpringBundle("org.zenoss.dropwizardspring.testclasses");
//        sb.setDefaultProfiles("broken");
//        Configuration config = new TestConfiguration();
//        Environment environment = mock(Environment.class);
//        ExecutorService service = mock(ExecutorService.class);
//        when(environment.managedExecutorService(anyString(), anyInt(), anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(service);
//        sb.run(config, environment);
//    }
//
//    @Test
//    public void testEventBus() throws Exception {
//        SpringBundle sb = new SpringBundle("org.zenoss.dropwizardspring.testclasses");
//        sb.setDefaultProfiles();
//        Configuration config = new TestConfiguration();
//        Environment environment = mock(Environment.class);
//        ExecutorService service = mock(ExecutorService.class);
//        when(environment.managedExecutorService(anyString(), anyInt(), anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(service);
//        sb.run(config, environment);
//
//        TestEventBus testEventBus = sb.applicationContext.getBean(TestEventBus.class);
//        assertNotNull(testEventBus);
//        assertNotNull(testEventBus.getSyncEventBus());
//        assertNotNull(testEventBus.getAsynEventBus());
//        assertThat(testEventBus.getSyncEventBus(), not(instanceOf(AsyncEventBus.class)));
//        assertThat(testEventBus.getAsynEventBus(), instanceOf(AsyncEventBus.class));
//    }
}
