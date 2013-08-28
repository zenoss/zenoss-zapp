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

import io.ifar.dropwizard.shiro.ShiroConfiguration;
import org.zenoss.app.config.ProxyConfiguration;
import org.zenoss.dropwizardspring.SpringConfiguration;
import org.zenoss.dropwizardspring.eventbus.EventBusConfiguration;
import org.zenoss.dropwizardspring.websockets.WebSocketConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

import javax.validation.Valid;


/**
 * Base class for App configurations. Implementations should add configuration properties and optionally
 * sub configuration objects.
 */

public abstract class AppConfiguration extends Configuration implements SpringConfiguration {
	@JsonProperty
	private ProxyConfiguration proxyConfiguration = new ProxyConfiguration();

	@JsonProperty
    private WebSocketConfiguration webSocketConfiguration = new WebSocketConfiguration();

    @JsonProperty
    private EventBusConfiguration eventBusConfiguration = new EventBusConfiguration();

    @Valid
    @JsonProperty("shiro_configuration")
    private ShiroConfiguration shiroConfiguration = new ShiroConfiguration();

    public ShiroConfiguration getShiroConfiguration() {
        return shiroConfiguration;
    }

	public ProxyConfiguration getProxyConfiguration() {
		return proxyConfiguration;
	}

    public WebSocketConfiguration getWebSocketConfiguration() {
        return webSocketConfiguration;
    }

    public EventBusConfiguration getEventBusConfiguration() {
        return eventBusConfiguration;
    }
    
    public void setProxyConfiguration(ProxyConfiguration proxyConfiguration) {
    	this.proxyConfiguration = proxyConfiguration;
    }

    public void setWebSocketConfiguration(WebSocketConfiguration webSocketConfiguration) {
        this.webSocketConfiguration = webSocketConfiguration;
    }

    public void setEventBusConfiguration(EventBusConfiguration eventBusConfiguration) {
        this.eventBusConfiguration = eventBusConfiguration;
    }
}
