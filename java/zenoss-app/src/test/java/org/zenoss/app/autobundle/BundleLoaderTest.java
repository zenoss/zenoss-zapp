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

import org.scannotation.ClasspathUrlFinder;

import java.net.URL;

public class BundleLoaderTest {

    @org.junit.Test
    public void testLoadBundles() throws Exception {

        ClasspathUrlFinder cuf = new ClasspathUrlFinder();
        URL[] x = cuf.findClassPaths();

        for (URL u: x){
            System.out.println(u);
        }

    }
}
