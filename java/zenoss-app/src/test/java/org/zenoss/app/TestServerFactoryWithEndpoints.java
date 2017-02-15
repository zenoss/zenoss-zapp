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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.springframework.stereotype.Component;
import org.zenoss.app.config.WebsocketConfiguration;

import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import org.junit.Test;

import java.io.IOException;
import java.lang.IllegalStateException;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestServerFactoryWithEndpoints {

	@Test()
	public void testAddEndpoint() {
		ServerFactory baseFactory = mock(ServerFactory.class);
		WebsocketConfiguration config = mock(WebsocketConfiguration.class);
		ServerFactoryWithEndpoints factory = new ServerFactoryWithEndpoints(baseFactory, config);

		assertNull(factory.getEndpoint("/foo/bar"));

		@ServerEndpoint("/foo/bar") class foobar {}
		factory.addEndpoint(foobar.class);

		ServerFactoryWithEndpoints.Endpoint ep = factory.getEndpoint("/foo/bar");
		assertNotNull(ep);
		assertEquals(ep.path, "/foo/bar");
		assertEquals(ep.name, foobar.class.getName());
	}

	@Test()
	public void testAddSpringEndpoint() {
		ServerFactory baseFactory = mock(ServerFactory.class);
		WebsocketConfiguration config = mock(WebsocketConfiguration.class);
		ServerFactoryWithEndpoints factory = new ServerFactoryWithEndpoints(baseFactory, config);

		assertNull(factory.getEndpoint("/foo/bar"));

		@Component @ServerEndpoint("/foo/bar") class foobar {}
		ServerEndpoint sep = foobar.class.getAnnotation(ServerEndpoint.class);
		ServerEndpointConfig cfg = ServerEndpointConfig.Builder.create(foobar.class, sep.value()).build();
		factory.addEndpoint(cfg);

		ServerFactoryWithEndpoints.Endpoint ep = factory.getEndpoint("/foo/bar");
		assertNotNull(ep);
		assertEquals(ep.path, "/foo/bar");
		assertEquals(ep.name, foobar.class.getName());
	}

	@Test()
	public void testAddMultipleEndpoints() {
		ServerFactory baseFactory = mock(ServerFactory.class);
		WebsocketConfiguration config = mock(WebsocketConfiguration.class);
		ServerFactoryWithEndpoints factory = new ServerFactoryWithEndpoints(baseFactory, config);

		@ServerEndpoint("/foo/bar") class foobar {}
		@ServerEndpoint("/foo/bar/baz") class foobarbaz {}
		factory.addEndpoint(foobar.class);
		factory.addEndpoint(foobarbaz.class);
		assertNotEquals(factory.getEndpoint("foo/bar"), factory.getEndpoint("/foo/bar/baz"));
		assertNotNull(factory.getEndpoint("/foo/bar/baz"));
	}

	@Test(expected = IllegalStateException.class)
	public void testAddDuplicateEndpoints() {
		ServerFactory baseFactory = mock(ServerFactory.class);
		WebsocketConfiguration config = mock(WebsocketConfiguration.class);
		ServerFactoryWithEndpoints factory = new ServerFactoryWithEndpoints(baseFactory, config);

		@ServerEndpoint("/foo/bar") class foobar0 {}
		@ServerEndpoint("/foo/bar") class foobar1 {}
		factory.addEndpoint(foobar0.class);
		factory.addEndpoint(foobar1.class);
	}

	@Test()
	public void testBuildAddsEndpoint() throws ServletException, DeploymentException {
		Server server = mock(Server.class);

		MutableServletContextHandler appContext = mock(MutableServletContextHandler.class);
		when(appContext.getServer()).thenReturn(server);

		MutableServletContextHandler adminContext = mock(MutableServletContextHandler.class);

		Environment env = mock(Environment.class);
		when(env.getApplicationContext()).thenReturn(appContext);
		when(env.getAdminContext()).thenReturn(adminContext);

		ServerFactory baseFactory = mock(ServerFactory.class);
		when(baseFactory.build(env)).thenReturn(server);

		ServerContainer serverContainer = mock(ServerContainer.class);
		ServerFactoryWithEndpoints.WebSocketServerContainerInitializerWrapper initializer =
				mock(ServerFactoryWithEndpoints.WebSocketServerContainerInitializerWrapper.class);
		when(initializer.configureContext(appContext)).thenReturn(serverContainer);

		WebsocketConfiguration config = new WebsocketConfiguration();
		ServerFactoryWithEndpoints factory = new ServerFactoryWithEndpoints(baseFactory, config, initializer);

		@ServerEndpoint("/foo/bar") class foobar {}
		factory.addEndpoint(foobar.class);

		factory.build(env);

		verify(appContext).setServer(server);
		verify(adminContext).setServer(server);
		verify(serverContainer).addEndpoint(foobar.class);
	}


	@Test
	public void testBuildUsesConfiguration() throws ServletException, DeploymentException, IOException {
		Server server = mock(Server.class);

		MutableServletContextHandler appContext = mock(MutableServletContextHandler.class);
		when(appContext.getServer()).thenReturn(server);

		Environment env = mock(Environment.class);
		when(env.getApplicationContext()).thenReturn(appContext);
		when(env.getAdminContext()).thenReturn(mock(MutableServletContextHandler.class));

		ServerFactory baseFactory = mock(ServerFactory.class);
		when(baseFactory.build(env)).thenReturn(server);

		ServerContainer serverContainer = mock(ServerContainer.class);
		ServerFactoryWithEndpoints.WebSocketServerContainerInitializerWrapper initializer =
				mock(ServerFactoryWithEndpoints.WebSocketServerContainerInitializerWrapper.class);
		when(initializer.configureContext(appContext)).thenReturn(serverContainer);

		String json = "{" +
				"\"maxSessionIdleTimeout\":123," +
				"\"asyncSendTimeout\":234," +
				"\"maxBinaryMessageBufferSize\":345," +
				"\"maxTextMessageBufferSize\":456" +
				"}";
		ObjectMapper om = new ObjectMapper();
		WebsocketConfiguration config = om.readValue(om.getFactory().createParser(json), WebsocketConfiguration.class);
		ServerFactoryWithEndpoints factory = new ServerFactoryWithEndpoints(baseFactory, config, initializer);

		factory.build(env);

		verify(serverContainer).setDefaultMaxSessionIdleTimeout(123);
		verify(serverContainer).setAsyncSendTimeout(234);
		verify(serverContainer).setDefaultMaxBinaryMessageBufferSize(345);
		verify(serverContainer).setDefaultMaxTextMessageBufferSize(456);
	}

}
