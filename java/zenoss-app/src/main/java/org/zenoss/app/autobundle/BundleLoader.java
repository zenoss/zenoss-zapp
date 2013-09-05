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
import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import com.sun.jersey.spi.scanning.AnnotationScannerListener;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import org.zenoss.app.annotations.Bundle;

import java.io.IOException;
import java.util.Set;


/**
 * Scan packages for classes annotated with {@link Bundle} and load them into Dropwizard
 */
public final class BundleLoader {

    public void loadBundles(Bootstrap bootstrap, Class c, String... packages) throws Exception {
        for (Class clz : this.findBundles(packages)) {
            Object o = clz.newInstance();
            registerBundle(o, bootstrap, c);
        }
    }

    Set<Class<?>> findBundles(String... packages) throws IOException {
        PackageNamesScanner scanner = new PackageNamesScanner(packages);
        AnnotationScannerListener listener = new AnnotationScannerListener(Bundle.class);
        scanner.scan(listener);
        return listener.getAnnotatedClasses();
    }

    void registerBundle(Object o, Bootstrap bootstrap, Class c) {
        if (o instanceof AutoBundle) {
            AutoBundle ab = (AutoBundle) o;
            checkConfigType(c, ab);
            bootstrap.addBundle(ab.getBundle());

        } else if (o instanceof AutoConfiguredBundle) {
            AutoConfiguredBundle ab = (AutoConfiguredBundle) o;
            checkConfigType(c, ab);
            bootstrap.addBundle(ab.getBundle(bootstrap));

        } else if (o instanceof ConfiguredBundle) {
            bootstrap.addBundle((ConfiguredBundle) o);
        } else if (o instanceof com.yammer.dropwizard.Bundle) {
            bootstrap.addBundle((com.yammer.dropwizard.Bundle) o);
        } else {
            throw new UnknownBundle("Unknown bundle type " + o.getClass().getName());
        }
    }

    private void checkConfigType(Class c, AutoBundle ab) {
        checkConfigAssignment(c, ab.getRequiredConfig());
    }


    private void checkConfigType(Class c, AutoConfiguredBundle ab) {
        checkConfigAssignment(c, ab.getRequiredConfig());
    }

    private void checkConfigAssignment(Class c, Optional<Class> required) {
        if (required.isPresent() && !required.get().isAssignableFrom(c)) {
            throw new BundleLoadException("Configuration " + c.getName() + " does not implement required " + required.get());
        }
    }

}
