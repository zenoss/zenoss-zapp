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

public final class BundleLoader {

    public void loadBundles(){
        ClasspathUrlFinder cuf = new ClasspathUrlFinder();
        cuf.findClassPaths("blam");
    }

}
