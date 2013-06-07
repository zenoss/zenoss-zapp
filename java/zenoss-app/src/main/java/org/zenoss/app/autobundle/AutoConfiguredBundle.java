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

import com.yammer.dropwizard.ConfiguredBundle;

/**
 * Created with IntelliJ IDEA.
 * User: jplouis
 * Date: 6/7/13
 * Time: 11:08 AM
 * To change this template use File | Settings | File Templates.
 */
public interface AutoConfiguredBundle{

    ConfiguredBundle getBundle();

    Class getConfigImplements();


}
