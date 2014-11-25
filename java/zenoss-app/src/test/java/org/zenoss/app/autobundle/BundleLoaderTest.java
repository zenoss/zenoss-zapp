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


package org.zenoss.app.autobundle;

import com.google.common.base.Optional;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BundleLoaderTest {

    @Test
    public void testLoadBundles() throws Exception {

        BundleLoader bl = new BundleLoader();
        Bootstrap bootstrap = mock(Bootstrap.class);
        bl.loadBundles(bootstrap, FakeAppConfig.class, "org.zenoss.app.autobundle");

    }

    @Test
    public void testFindBundles() throws Exception {

        BundleLoader bl = new BundleLoader();
        Set<Class<?>> bundles = bl.findBundles("org.zenoss.app.autobundle");
        Assert.assertEquals(1, bundles.size());
        Assert.assertTrue(bundles.contains(FakeBundle.class));
    }

    @Test()
    public void testRegisterBundle() {
        BundleLoader bl = new BundleLoader();
        Bootstrap bootstrap = mock(Bootstrap.class);
        Bundle bundle = mock(Bundle.class);
        bl.registerBundle(bundle, bootstrap, FakeAppConfig.class);
    }

    @Test()
    public void testRegisterConfiguredBundle() {
        BundleLoader bl = new BundleLoader();
        Bootstrap bootstrap = mock(Bootstrap.class);
        ConfiguredBundle configuredBundle = mock(ConfiguredBundle.class);
        bl.registerBundle(configuredBundle, bootstrap, FakeAppConfig.class);

    }

    @Test()
    public void testRegisterAutoBundle() {
        BundleLoader bl = new BundleLoader();
        Bootstrap bootstrap = mock(Bootstrap.class);
        Bundle bundle = mock(Bundle.class);

        AutoBundle autoBundle = mock(AutoBundle.class);
        when(autoBundle.getBundle()).thenReturn(bundle);
        when(autoBundle.getRequiredConfig()).thenReturn(Optional.<Class>absent());

        bl.registerBundle(autoBundle, bootstrap, FakeAppConfig.class);

    }

    @Test()
    public void testRegisterAutoBundle_withConfig() {
        BundleLoader bl = new BundleLoader();
        Bootstrap bootstrap = mock(Bootstrap.class);
        Bundle bundle = mock(Bundle.class);

        AutoBundle autoBundle = mock(AutoBundle.class);
        when(autoBundle.getBundle()).thenReturn(bundle);
        when(autoBundle.getRequiredConfig()).thenReturn(Optional.<Class>of(FakeConfig.class));

        bl.registerBundle(autoBundle, bootstrap, FakeAppConfig.class);

    }

    @Test(expected = BundleLoadException.class)
    public void testRegisterAutoBundle_withConfigError() {
        BundleLoader bl = new BundleLoader();
        Bootstrap bootstrap = mock(Bootstrap.class);
        Bundle bundle = mock(Bundle.class);

        AutoBundle autoBundle = mock(AutoBundle.class);
        when(autoBundle.getBundle()).thenReturn(bundle);
        when(autoBundle.getRequiredConfig()).thenReturn(Optional.<Class>of(this.getClass()));

        bl.registerBundle(autoBundle, bootstrap, FakeAppConfig.class);

    }

    @Test()
    public void testRegisterAutoConfiguredBundle() {
        BundleLoader bl = new BundleLoader();
        Bootstrap bootstrap = mock(Bootstrap.class);
        ConfiguredBundle configuredBundle = mock(ConfiguredBundle.class);

        AutoConfiguredBundle autoConfiguredBundle = mock(AutoConfiguredBundle.class);
        when(autoConfiguredBundle.getBundle(bootstrap)).thenReturn(configuredBundle);
        when(autoConfiguredBundle.getRequiredConfig()).thenReturn(Optional.<Class>absent());

        bl.registerBundle(autoConfiguredBundle, bootstrap, FakeAppConfig.class);

    }

    @Test()
    public void testRegisterAutoConfiguredBundle_withConfig() {
        BundleLoader bl = new BundleLoader();
        Bootstrap bootstrap = mock(Bootstrap.class);
        ConfiguredBundle configuredBundle = mock(ConfiguredBundle.class);

        AutoConfiguredBundle autoConfiguredBundle = mock(AutoConfiguredBundle.class);
        when(autoConfiguredBundle.getBundle(bootstrap)).thenReturn(configuredBundle);
        when(autoConfiguredBundle.getRequiredConfig()).thenReturn(Optional.<Class>of(FakeConfig.class));

        bl.registerBundle(autoConfiguredBundle, bootstrap, FakeAppConfig.class);
    }

    @Test(expected = BundleLoadException.class)
    public void testRegisterAutoConfiguredBundle_withConfigError() {
        BundleLoader bl = new BundleLoader();
        Bootstrap bootstrap = mock(Bootstrap.class);
        ConfiguredBundle configuredBundle = mock(ConfiguredBundle.class);

        AutoConfiguredBundle autoConfiguredBundle = mock(AutoConfiguredBundle.class);
        when(autoConfiguredBundle.getBundle(bootstrap)).thenReturn(configuredBundle);
        when(autoConfiguredBundle.getRequiredConfig()).thenReturn(Optional.<Class>of(this.getClass()));

        bl.registerBundle(autoConfiguredBundle, bootstrap, FakeAppConfig.class);
    }

    @Test(expected = UnknownBundle.class)
    public void testRegisterBundle_error() {
        BundleLoader bl = new BundleLoader();
        Bootstrap bootstrap = mock(Bootstrap.class);
        bl.registerBundle(new Object(), bootstrap, FakeAppConfig.class);
    }


}
