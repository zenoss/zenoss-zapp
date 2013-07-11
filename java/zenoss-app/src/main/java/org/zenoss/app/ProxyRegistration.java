package org.zenoss.app;

import java.net.Socket;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.zenoss.app.config.ProxyConfiguration;
import org.zenoss.dropwizardspring.annotations.Resource;
import org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener;

import redis.clients.jedis.Jedis;

import com.yammer.dropwizard.lifecycle.Managed;

@org.zenoss.dropwizardspring.annotations.Managed
public class ProxyRegistration implements Managed {
	private static final String REDIS_HOST_KEY = "zapp.autoreg.host";
	private static final String REDIS_PORT_KEY = "zapp.autoreg.port";	
	private static final String REDIS_SCRIPTS_KEY = "scripts";
	private static final String REDIS_REGISTER_SCRIPT = "register";
	private static final String REDIS_UNREGISTER_SCRIPT = "unregister";
		
	private boolean enabled = false;
	private String redisHost;
	private int redisPort;		
	private String httpServer;
	private String wsServer;
	
	@Autowired
	private AppConfiguration configuration;
	
	@Autowired 
	private ApplicationContext applicationContext;
		
	private void setUp() throws Exception {
		final String SERVER_ADDRESS_FORMAT = "%s://%s:%d";
		
		// Configure the redis connection
		String redisHost = System.getenv(REDIS_HOST_KEY);
		String redisPort = System.getenv(REDIS_PORT_KEY);
		if (redisHost == null || redisPort == null) {
			// Redis is not defined
			return;
		}
		this.redisHost = redisHost;
		this.redisPort = Integer.parseInt(redisPort);
		
		ProxyConfiguration proxyConf = configuration.getProxyConfiguration();
		if (proxyConf == null) {
			// Proxy is not defined
			return;
		}
		
		// Get the localhost ip by pinging the proxy
		Socket socket = null;
		try {
			socket = new Socket(proxyConf.getHostname(), proxyConf.getPort());
			String localAddress = socket.getLocalAddress().getHostAddress();
			int port = configuration.getHttpConfiguration().getPort();
			boolean isSsl = configuration.getHttpConfiguration().isSslConfigured();
			
			final String HTTP_PROTOCOL = "http" + (isSsl?"s":"");
			final String WS_PROTOCOL = "ws" + (isSsl?"s":"");
			
			this.httpServer = String.format(SERVER_ADDRESS_FORMAT, HTTP_PROTOCOL, localAddress, port);
			this.wsServer = String.format(SERVER_ADDRESS_FORMAT, WS_PROTOCOL, localAddress, port);						
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e) {}
			}			
		}
		
		this.enabled = true;
	}
		
	@Override
	public void start() throws Exception {
		setUp();
		
		Jedis jedis = null;
		
		if (!enabled) return;
		
		try {
			jedis = new Jedis(this.redisHost, this.redisPort);
			jedis.connect();
			
			// Get all http resources and map them to the proxy
			final Map<String, Object> resources = applicationContext.getBeansWithAnnotation(Resource.class);
			for (final Object resource: resources.values()) {
				registerHTTP(jedis, resource);
			}
			
			// Get all ws resources and map them to the proxy
			final Map<String, Object> websockets = applicationContext.getBeansWithAnnotation(WebSocketListener.class);
			for (final Object websocket: websockets.values()) {
				registerWS(jedis, websocket);
			}
								
		} finally {
			if (jedis != null) {
				try {
					jedis.disconnect();
				} catch (Exception e) {};
			}			
		}
	}
	
	private void register(Jedis j, String appname, String server) {
		String sha1 = j.hget(REDIS_SCRIPTS_KEY, REDIS_REGISTER_SCRIPT);
		if (sha1 != null) {			
			j.evalsha(sha1, 1, appname, server);			
		} // throw exception if false		
	}
	
	private void registerHTTP(Jedis j, Object resource) {
		Resource r = resource.getClass().getAnnotation(Resource.class);
		register(j, r.name(), httpServer);
	}
	
	private void registerWS(Jedis j, Object websocket) {
		WebSocketListener w = websocket.getClass().getAnnotation(WebSocketListener.class);
		register(j, w.name(), wsServer);
	}
		
	@Override
	public void stop() throws Exception {
		Jedis jedis = null;
		
		if (!enabled) return;
		
		try {
			jedis = new Jedis(this.redisHost, this.redisPort);
			jedis.connect();
			
			// Get all http resources and map them to the proxy
			final Map<String, Object> resources = applicationContext.getBeansWithAnnotation(Resource.class);
			for (final Object resource: resources.values()) {
				unregisterHTTP(jedis, resource);
			}
			
			// Get all ws resources and map them to the proxy
			final Map<String, Object> websockets = applicationContext.getBeansWithAnnotation(WebSocketListener.class);
			for (final Object websocket: websockets.values()) {
				unregisterWS(jedis, websocket);
			}
		} finally {			
			if (jedis != null) {
				try {
					jedis.disconnect();
				} catch (Exception e) {};
			}			
		}
	}
	
	private void unregister(Jedis j, String appname, String server) {
		String sha1 = j.hget(REDIS_SCRIPTS_KEY, REDIS_UNREGISTER_SCRIPT);
		if (sha1 != null) {			
			j.evalsha(sha1, 1, appname, server);			
		} // throw exception if false		
	}
	
	private void unregisterHTTP(Jedis j, Object resource) {
		Resource r = resource.getClass().getAnnotation(Resource.class);
		unregister(j, r.name(), httpServer);
	}
	
	private void unregisterWS(Jedis j, Object websocket) {
		WebSocketListener w = websocket.getClass().getAnnotation(WebSocketListener.class);
		unregister(j, w.name(), wsServer);
	}
}
