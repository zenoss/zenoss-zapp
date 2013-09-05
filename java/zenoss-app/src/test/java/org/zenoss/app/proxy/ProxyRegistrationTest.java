package org.zenoss.app.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.zenoss.app.AppConfiguration;
import org.zenoss.app.proxy.exceptions.RegistrationException;
import org.zenoss.dropwizardspring.annotations.Resource;
import org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import com.yammer.dropwizard.config.HttpConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
public class ProxyRegistrationTest {
	
	// TODO: Create configuration and application context
	
	/*
	 * Test Cases:
	 * 1) redis not configured (OK)
	 * 2) proxy not configured (OK)
	 * 3) proxy not connect (throw IOException)
	 * 4) with ssl (check names of http and ws)
	 * 5) without ssl (check names of http and ws)
	 * 6) register + unregister * http + ws  
	 *    a. Script hash not found (throw <some>Exception)
	 *    b. Script not found (throw <some>Exception)
	 *    c. Script failure (throw <some>Exception)
	 *    d. Script successful (OK)
	 */
		
	@Test
	public void testNoSetup() throws Exception {
		ProxyRegistration spy;
		
		/* Redis not configured */
		spy = spy(new ProxyRegistration());	
		doReturn(null).when(spy).getRedisHost();
		doReturn(-1).when(spy).getRedisPort();		

		spy.start();
		assertFalse(spy.isEnabled());
		spy.stop();
		assertFalse(spy.isEnabled());
		
		/* Proxy not configured */
		spy = spy(new ProxyRegistration());
		doReturn("redishost").when(spy).getRedisHost();
		doReturn(8080).when(spy).getRedisPort();
		
		AppConfiguration config = mock(AppConfiguration.class);
		when(config.getProxyConfiguration()).thenReturn(null);
		doReturn(config).when(spy).getConfiguration();
		
		spy.start();
		assertFalse(spy.isEnabled());
		spy.stop();
		assertFalse(spy.isEnabled());
	}
	
	@Test(expected=IOException.class)
	public void testNoSocketConnect() throws Exception {
		ProxyRegistration spy = spy(new ProxyRegistration());		
		doReturn("redishost").when(spy).getRedisHost();
		doReturn(8080).when(spy).getRedisPort();
		doThrow(new IOException()).when(spy).getLocalAddress();

		spy.start();
	}
	
	@Test(expected=JedisConnectionException.class)
	public void testNoRedisConnectOnStart() throws Exception {
		ProxyRegistration spy = spy(new ProxyRegistration());
		doNothing().when(spy).setUp();
		doReturn(true).when(spy).isEnabled();
		
		Jedis jedis = spy(new Jedis("redisHost"));
		doThrow(new JedisConnectionException("TESTING CONNECTION EXCEPTION")).when(jedis).connect();
		doReturn(jedis).when(spy).initJedis();
		
		spy.start();
	}
	
	@Test(expected=JedisConnectionException.class)
	public void testNoRedisConnectOnStop() throws Exception {
		ProxyRegistration spy = spy(new ProxyRegistration());
		doReturn(true).when(spy).isEnabled();
		
		Jedis jedis = spy(new Jedis("redisHost", 8080));
		doThrow(new JedisConnectionException("TESTING CONNECTION EXCEPTION")).when(jedis).connect();
		doReturn(jedis).when(spy).initJedis();
		
		spy.stop();
	}
	
	@Test(expected=RegistrationException.class)
	public void testStartWithNullScriptHash() throws Exception {
		@Path("/path/to/resource")
		@Resource(name="FakeResource")
		final class FakeResource {}
				
		ProxyRegistration spy;
				
		/* Using resources */
		spy = spy(new ProxyRegistration());
		doNothing().when(spy).setUp();
		doReturn(true).when(spy).isEnabled();
		
		Jedis jedis = spy(new Jedis("redisHost"));
		doNothing().when(jedis).connect();
		doNothing().when(jedis).disconnect();		
		doReturn(null).when(jedis).hget("scripts", "register");
		doReturn(jedis).when(spy).initJedis();
		
		ApplicationContext context = mock(ApplicationContext.class);
		Map<String, Object> resources = new HashMap<String, Object>();
		resources.put("FakeResource", new FakeResource());
		when(context.getBeansWithAnnotation(Resource.class)).thenReturn(resources);		
		doReturn(context).when(spy).getApplicationContext();
		
		spy.start();		
	}
	
	@Test(expected=RegistrationException.class)
	public void testStopNoScriptHash() throws Exception {
		@Path("/path/to/websocket")
		@WebSocketListener(name="FakeWebSocket")
		final class FakeWebSocket {}
		
		ProxyRegistration spy;
		
		/* Using websockets */
		spy = spy(new ProxyRegistration());
		doNothing().when(spy).setUp();
		doReturn(true).when(spy).isEnabled();
		
		Jedis jedis = spy(new Jedis("redisHost"));			
		doNothing().when(jedis).connect();
		doNothing().when(jedis).disconnect();
		doReturn(null).when(jedis).hget("scripts", "unregister");
		doReturn(jedis).when(spy).initJedis();
		
		ApplicationContext context = mock(ApplicationContext.class);
		Map<String, Object> websockets = new HashMap<String, Object>();
		websockets.put("FakeWebSocket", new FakeWebSocket());
		when(context.getBeansWithAnnotation(WebSocketListener.class)).thenReturn(websockets);		
		doReturn(context).when(spy).getApplicationContext();
		
		spy.stop();		
	}
	
	@Test(expected=JedisException.class)
	public void testStartScriptFailure() throws Exception {
		@Path("/path/to/websocket")
		@WebSocketListener(name="FakeWebSocket")
		final class FakeWebSocket {}
		
		final String SHA1 = "s0m3f4k3h45hk3yv4lu3";
		
		ProxyRegistration spy;
		
		/* Using websockets */
		spy = spy(new ProxyRegistration());
		doNothing().when(spy).setUp();
		doReturn(true).when(spy).isEnabled();
		
		Jedis jedis = spy(new Jedis("redisHost"));
		doNothing().when(jedis).connect();
		doNothing().when(jedis).disconnect();
		doReturn(SHA1).when(jedis).hget("scripts", "register");
		doThrow(JedisException.class).when(jedis).evalsha(SHA1);
		doReturn(jedis).when(spy).initJedis();
		
		ApplicationContext context = mock(ApplicationContext.class);
		Map<String, Object> websockets = new HashMap<String, Object>();
		websockets.put("FakeWebSocket", new FakeWebSocket());
		when(context.getBeansWithAnnotation(WebSocketListener.class)).thenReturn(websockets);
		doReturn(context).when(spy).getApplicationContext();
		
		spy.start();				
	}
	
	@Test(expected=JedisException.class)
	public void testStopScriptFailure() throws Exception {
		@Path("/path/to/resource")
		@Resource(name="FakeResource")
		final class FakeResource {}
		
		final String SHA1 = "s0m3f4k3h45hk3yv4lu3";
		
		ProxyRegistration spy;
		
		/* Using resources */
		spy = spy(new ProxyRegistration());
		doNothing().when(spy).setUp();
		doReturn(true).when(spy).isEnabled();
		
		Jedis jedis = spy(new Jedis("redisHost"));
		doNothing().when(jedis).connect();
		doNothing().when(jedis).disconnect();
		doReturn(SHA1).when(jedis).hget("scripts", "unregister");
		doThrow(JedisException.class).when(jedis).evalsha(SHA1);
		doReturn(jedis).when(spy).initJedis();
		
		ApplicationContext context = mock(ApplicationContext.class);
		Map<String, Object> resources = new HashMap<String, Object>();
		resources.put("FakeResource", new FakeResource());
		when(context.getBeansWithAnnotation(Resource.class)).thenReturn(resources);
		doReturn(context).when(spy).getApplicationContext();
		
		spy.stop();		
	}
	
	@Test
	public void testStartStopSuccess() throws Exception {
		@Path("/path/to/resource")
		@Resource(name="FakeResource")
		final class FakeResource {}
		
		@Path("/path/to/websocket")
		@WebSocketListener(name="FakeWebSocket")
		final class FakeWebSocket {}
		
		final String SHA1 = "s0m3f4k3h45hk3yv4lu3";
		
		ProxyRegistration spy;
		
		AppConfiguration conf;
		HttpConfiguration httpconf;
				
		Jedis jedis = spy(new Jedis("redisHost"));
		doNothing().when(jedis).connect();
		doNothing().when(jedis).disconnect();
		doReturn(SHA1).when(jedis).hget("scripts", "register");
		doReturn(SHA1).when(jedis).hget("scripts", "unregister");
		doReturn("/api/FakeResource").when(jedis).evalsha(SHA1, 1, "/api/FakeResource", "http://localhost:8080/path/to/resource");
		doReturn("/api/FakeResource").when(jedis).evalsha(SHA1, 1, "/api/FakeResource", "https://localhost:8080/path/to/resource");
		doReturn("/ws/FakeWebSocket").when(jedis).evalsha(SHA1, 1, "/ws/FakeWebSocket", "ws://localhost:8080/path/to/websocket");
		doReturn("/ws/FakeWebSocket").when(jedis).evalsha(SHA1, 1, "/ws/FakeWebSocket", "wss://localhost:8080/path/to/websocket");
		
		ApplicationContext context = mock(ApplicationContext.class);
		Map<String, Object> resources = new HashMap<String, Object>();
		resources.put("FakeResource", new FakeResource());
		when(context.getBeansWithAnnotation(Resource.class)).thenReturn(resources);
		Map<String, Object> websockets = new HashMap<String, Object>();
		websockets.put("FakeWebSocket", new FakeWebSocket());
		when(context.getBeansWithAnnotation(WebSocketListener.class)).thenReturn(websockets);
						
		/* Without SSL */		
		spy = spy(new ProxyRegistration());
		doReturn("redisHost").when(spy).getRedisHost();
		doReturn(6379).when(spy).getRedisPort();
		doReturn("localhost").when(spy).getLocalAddress();
		
		conf = mock(AppConfiguration.class);
		httpconf = mock(HttpConfiguration.class);
		when(httpconf.getPort()).thenReturn(8080);
		when(httpconf.isSslConfigured()).thenReturn(false);
		when(conf.getHttpConfiguration()).thenReturn(httpconf);
		
		doReturn(conf).when(spy).getConfiguration();
		doReturn(jedis).when(spy).initJedis();
		doReturn(context).when(spy).getApplicationContext();
		
		spy.start();
		assertEquals(spy.getHttpServer(), "http://localhost:8080");
		assertEquals(spy.getWsServer(), "ws://localhost:8080");
		spy.stop();
				
		/* With SSL */
		spy = spy(new ProxyRegistration());
		doReturn("redisHost").when(spy).getRedisHost();
		doReturn(6379).when(spy).getRedisPort();
		doReturn("localhost").when(spy).getLocalAddress();
		
		conf = mock(AppConfiguration.class);
		httpconf = mock(HttpConfiguration.class);
		when(httpconf.getPort()).thenReturn(8080);
		when(httpconf.isSslConfigured()).thenReturn(true);
		when(conf.getHttpConfiguration()).thenReturn(httpconf);
		
		doReturn(conf).when(spy).getConfiguration();
		doReturn(jedis).when(spy).initJedis();
		doReturn(context).when(spy).getApplicationContext();
		
		spy.start();
		assertEquals(spy.getHttpServer(), "https://localhost:8080");
		assertEquals(spy.getWsServer(), "wss://localhost:8080");
		spy.stop();
		
	}
}
