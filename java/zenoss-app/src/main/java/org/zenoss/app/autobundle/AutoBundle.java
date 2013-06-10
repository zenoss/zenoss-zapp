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
import com.yammer.dropwizard.Bundle;

/**
 *
 * Interface for configuring a drop wizard bundle that can be automatically loaded.
 *
 */
public interface AutoBundle {

    /**
     * Get the initialized bundle to be loaded.
     *
     * @return Bundle to be added
     */
    Bundle getBundle();

    /**
     * Asserts that the class returned should be the same, a superclass or a super interface of the App Configuration
     *
     * @return Class Configuration type expected by the bundle
     */
    Optional<Class> getRequiredConfig();

}
