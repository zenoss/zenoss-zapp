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
import com.google.common.collect.Lists;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import eu.infomas.annotation.AnnotationDetector;
import eu.infomas.annotation.AnnotationDetector.TypeReporter;
import org.zenoss.app.annotations.Bundle;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;


/**
 *
 * Scan packages for classes annotated with {@link Bundle} and load them into Dropwizard
 *
 */
public final class BundleLoader{
    private static class BundleReporter implements TypeReporter {

        private List<String> foundClasses = Lists.newArrayList();

        private final Class<? extends Annotation>[] annotations;

        public BundleReporter(Class<? extends Annotation>... annotations) {
            this.annotations = annotations;
        }

        @Override
        public void reportTypeAnnotation(Class<? extends Annotation> aClass, String s) {
            this.foundClasses.add(s);
        }

        @Override
        public Class<? extends Annotation>[] annotations() {
            return this.annotations;
        }
    }


    public void loadBundles(Bootstrap bootstrap, Class c, String... packages) throws Exception {
        for (String clzName : this.findBundles(packages)) {
            Class clz = Class.forName(clzName);
            Object o = clz.newInstance();
            registerBundle(o, bootstrap, c);
        }
    }

    List<String> findBundles(String... packages) throws IOException {
        BundleReporter br = new BundleReporter(Bundle.class);
        AnnotationDetector ad = new AnnotationDetector(br);
        ad.detect(packages);
        return br.foundClasses;
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
