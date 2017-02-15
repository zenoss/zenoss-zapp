// Copyright 2017 The Serviced Authors.
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

package org.zenoss.app;

import com.google.common.collect.Maps;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenoss.app.config.WebsocketConfiguration;

import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.lang.Integer;
import java.lang.Long;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ServerFactoryWithEndpoints implements ServerFactory {
	private static final Logger log = LoggerFactory.getLogger(ServerFactoryWithEndpoints.class);

	public interface WebSocketServerContainerInitializerWrapper{
		ServerContainer configureContext(MutableServletContextHandler ctx) throws ServletException;
	}
    private WebSocketServerContainerInitializerWrapper initializer;
    private ServerFactory original;
    private WebsocketConfiguration configuration;
	private Map<String, Endpoint> endpoints = Maps.newLinkedHashMap();

	public ServerFactoryWithEndpoints(ServerFactory serverFactory, WebsocketConfiguration configuration) {
		this.original = serverFactory;
		this.configuration = configuration;
		this.initializer = (WebSocketServerContainerInitializerWrapper) new DefaultWebSocketServerContainerInitializerWrapper();
	}

    public ServerFactoryWithEndpoints(ServerFactory serverFactory, WebsocketConfiguration configuration,
									  WebSocketServerContainerInitializerWrapper initializer) {
        this.original = serverFactory;
        this.configuration = configuration;
		this.initializer = initializer;
    }

    @Override
    public Server build(Environment environment) {
        Server server = original.build(environment);
        environment.getApplicationContext().setServer(server);
        environment.getAdminContext().setServer(server);

        ServerContainer serverContainer;
        try {
        	serverContainer = initializer.configureContext(environment.getApplicationContext());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure websocket context", e);
        }

        applyConfiguration(serverContainer, configuration);

		registerEndpoints(serverContainer, endpoints.values());

		return server;
    }

    @Override
    public void configure(Environment e) {
    }

    public void addEndpoint(ServerEndpointConfig e) {
		Endpoint ep = new Endpoint(e);
		addEndpoint(ep);
    }

    public void addEndpoint(Class<?> e) {
		Endpoint ep = new Endpoint(e);
		addEndpoint(ep);
    }

	public Endpoint getEndpoint(String path) {
    	return endpoints.get(path);
	}

	private void addEndpoint(Endpoint e) {
		String path = e.path;
		Endpoint existing = endpoints.get(path);
		if (existing == null) {
			endpoints.put(path, e);
		} else {
			throw new IllegalStateException(String.format(
						"Registering %s failed: path \"%s\" aleady registered by %s", 
						e.name, path, existing.name));
		}
	}

    private void applyConfiguration(ServerContainer serverContainer, WebsocketConfiguration configuration) {
		Long longVal;
		Integer intVal;
        longVal = configuration.getMaxSessionIdleTimeout();
		if (longVal != null) { 
            serverContainer.setDefaultMaxSessionIdleTimeout(longVal);
        }
		longVal = configuration.getAsyncSendTimeout();
		if (longVal != null) {
			serverContainer.setAsyncSendTimeout(longVal);
		}
		intVal = configuration.getMaxBinaryMessageBufferSize();
		if (intVal != null) {
			serverContainer.setDefaultMaxBinaryMessageBufferSize(intVal);
		}
		intVal = configuration.getMaxTextMessageBufferSize();
		if (intVal != null) {
			serverContainer.setDefaultMaxTextMessageBufferSize(intVal);
		}
    }

	private void registerEndpoints(ServerContainer serverContainer, Collection<Endpoint> endpoints) {
		List<Endpoint> added = new ArrayList();
		for (Endpoint ep: endpoints) {
			try {
				ep.register(serverContainer);
				added.add(ep);
			} catch (DeploymentException exc) {
				log.error("Failed to add endpoint {}", ep, exc);
			}
		}

		String msg = "Registered websocket endpoints:";
		for (Endpoint ep: added) {
			msg += "\n" + String.format("\tGET\t%s (%s)", ep.path, ep.name);
		}
		log.info(msg);
	}

	static class Endpoint {
		public enum Type {CLASS, CONFIG}
		public String name;
		public String path;
		private Type type;
		private Object obj;

		public Endpoint (ServerEndpointConfig e){
			this.type = Type.CONFIG;
			this.path = e.getPath();
			this.name = e.getEndpointClass().getName();
			this.obj = e;
		}

		public Endpoint(Class<?> e) {
			this.type = Type.CLASS;
			this.path = e.getAnnotation(ServerEndpoint.class).value();
			this.name = e.getName();
			this.obj = e;
		}

		public void register(ServerContainer serverContainer) throws DeploymentException {
			switch (type) {
				case CONFIG:
					serverContainer.addEndpoint((ServerEndpointConfig)obj);
					break;
				case CLASS:
					serverContainer.addEndpoint((Class<?>)obj);
					break;
			}
		}
	}

	public class DefaultWebSocketServerContainerInitializerWrapper implements WebSocketServerContainerInitializerWrapper{
		@Override
		public ServerContainer configureContext(MutableServletContextHandler ctx) throws ServletException {
			return WebSocketServerContainerInitializer.configureContext(ctx);
		}
	}
}
