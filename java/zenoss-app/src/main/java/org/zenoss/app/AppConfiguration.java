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


import org.zenoss.app.config.ProxyConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;


/**
 * Base class for App configurations. Implementations should add configuration properties and optionally
 * sub configuration objects.
 */
public abstract class AppConfiguration extends Configuration {
	@JsonProperty
	private ProxyConfiguration proxy = new ProxyConfiguration();
	
	public ProxyConfiguration getProxyConfiguration() {
		return proxy;
	}

}
