package org.zenoss.app;

import java.net.Socket;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.zenoss.app.config.ProxyConfiguration;
import org.zenoss.dropwizardspring.annotations.Resource;
import org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import com.yammer.dropwizard.lifecycle.Managed;

@org.zenoss.dropwizardspring.annotations.Managed
public class ProxyRegistration implements Managed {
	private static final String REDIS_HOST_KEY = "zapp.autoreg.host";
	private static final String REDIS_PORT_KEY = "zapp.autoreg.port";	
	private static final String REDIS_HTTP_KEY_FORMAT = "%s:/app/%s";
	private static final String REDIS_WS_KEY_FORMAT = "%s:/ws/%s";
	private static final String REDIS_SERVER_POOL = "frontend";
	private static final String REDIS_DEAD_POOL = "dead";
	
	private boolean enabled = false;
	private String redisHost;
	private int redisPort;		
	private String httpServer;
	private String wsServer;
	
	@Autowired
	private AppConfiguration configuration;
	
	@Autowired 
	private ApplicationContext applicationContext;
		
	public ProxyRegistration() throws Exception {
		setUp();
	}
	
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
		Jedis jedis = null;
		Transaction transaction = null;
		
		if (!enabled) return;
		
		try {
			jedis = new Jedis(this.redisHost, this.redisPort);
			jedis.connect();
			transaction = jedis.multi();
			
			// Get all http resources and map them to the proxy
			final Map<String, Object> resources = applicationContext.getBeansWithAnnotation(Resource.class);
			for (final Object resource: resources.values()) {
				registerHTTP(transaction, resource);
			}
			
			// Get all ws resources and map them to the proxy
			final Map<String, Object> websockets = applicationContext.getBeansWithAnnotation(WebSocketListener.class);
			for (final Object websocket: websockets.values()) {
				registerWS(transaction, websocket);
			}
			
			transaction.exec();			
		} finally {
			if (transaction != null) {
				try {
					transaction.discard();
				} catch (Exception e) {}
			}
			
			if (jedis != null) {
				try {
					jedis.disconnect();
				} catch (Exception e) {};
			}			
		}
	}
	
	private void register(Transaction t, String appname, String server) {
		String endpoint = null;		
		if (server.startsWith("ws")) {
			endpoint = String.format(REDIS_WS_KEY_FORMAT, REDIS_SERVER_POOL, appname);
		} else { 
			endpoint = String.format(REDIS_HTTP_KEY_FORMAT, REDIS_SERVER_POOL, appname);
		}
				
		// Get all servers from the redis pool
		Response<List<String>> response = t.lrange(endpoint, 0, -1);
		List<String> serverList = response.get();
		
		if (serverList.isEmpty()) {
			t.rpush(endpoint, appname);
		}
		
		if (!serverList.contains(server)) {
			t.rpush(endpoint, server);			
		}				
	}
	
	private void registerHTTP(Transaction t, Object resource) {
		Resource r = resource.getClass().getAnnotation(Resource.class);
		register(t, r.value(), httpServer);
	}
	
	private void registerWS(Transaction t, Object websocket) {
		WebSocketListener w = websocket.getClass().getAnnotation(WebSocketListener.class);
		register(t, w.value(), wsServer);
	}
		
	@Override
	public void stop() throws Exception {
		Jedis jedis = null;
		Transaction transaction = null;
		
		if (!enabled) return;
		
		try {
			jedis = new Jedis(this.redisHost, this.redisPort);
			jedis.connect();
			transaction = jedis.multi();
			
			// Get all http resources and map them to the proxy
			final Map<String, Object> resources = applicationContext.getBeansWithAnnotation(Resource.class);
			for (final Object resource: resources.values()) {
				unregisterHTTP(transaction, resource);
			}
			
			// Get all ws resources and map them to the proxy
			final Map<String, Object> websockets = applicationContext.getBeansWithAnnotation(WebSocketListener.class);
			for (final Object websocket: websockets.values()) {
				unregisterWS(transaction, websocket);
			}
			
			transaction.exec();			
		} finally {
			if (transaction != null) {
				try {
					transaction.discard();
				} catch (Exception e) {}
			}
			
			if (jedis != null) {
				try {
					jedis.disconnect();
				} catch (Exception e) {};
			}			
		}
	}
	
	private void unregister(Transaction t, String appname, String server) {
		String endpoint = null;
		String deadpoint = null;
		
		if (server.startsWith("ws")) {
			endpoint = String.format(REDIS_WS_KEY_FORMAT, REDIS_SERVER_POOL, appname);
			deadpoint = String.format(REDIS_WS_KEY_FORMAT, REDIS_DEAD_POOL, appname);
		} else { 
			endpoint = String.format(REDIS_HTTP_KEY_FORMAT, REDIS_SERVER_POOL, appname);
			deadpoint = String.format(REDIS_HTTP_KEY_FORMAT, REDIS_DEAD_POOL, appname);
		}
		
		// Get all the servers from the redis pool
		Response<List<String>> response = t.lrange(endpoint, 0, -1);
		List<String> serverList = response.get();
		
		// Remove any instances of the server from the "dead" pool
		int index = serverList.indexOf(server);
		if (index > -1) {			
			t.lrem(deadpoint, 0, String.valueOf(index));
			t.lrem(endpoint, 0, server);
		}		
	}
	
	private void unregisterHTTP(Transaction t, Object resource) {
		Resource r = resource.getClass().getAnnotation(Resource.class);
		unregister(t, r.value(), httpServer);
	}
	
	private void unregisterWS(Transaction t, Object websocket) {
		WebSocketListener w = websocket.getClass().getAnnotation(WebSocketListener.class);
		unregister(t, w.value(), wsServer);
	}
}
