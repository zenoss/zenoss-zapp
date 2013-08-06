package org.zenoss.app.proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.zenoss.app.AppConfiguration;
import org.zenoss.app.config.ProxyConfiguration;
import org.zenoss.app.proxy.exceptions.RegistrationException;
import org.zenoss.dropwizardspring.annotations.Resource;
import org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener;

import redis.clients.jedis.Jedis;

import com.yammer.dropwizard.lifecycle.Managed;

/**
 * Auto-registers resource and websocket applications onto a centralized proxy 
 * server.  To enable, add the proxy configuration, with the host and port of
 * the proxy server and set environment variables for $zapp.autoreg.host and 
 * $zapp.autoreg.port that points to the proxy's redis database.
 * 
 * @author Summer Mousa <smousa@zenoss.com>
 * @see org.zenoss.app.config.ProxyConfiguration
 * 
 */
@org.zenoss.dropwizardspring.annotations.Managed
public class ProxyRegistration implements Managed {
	private static final Logger log = LoggerFactory.getLogger(ProxyRegistration.class);
	
	private static final String REDIS_HOST_KEY = "zapp.autoreg.host";
	private static final String REDIS_PORT_KEY = "zapp.autoreg.port";	
	private static final String REDIS_SCRIPTS_KEY = "scripts";
	private static final String REDIS_REGISTER_SCRIPT = "register";
	private static final String REDIS_UNREGISTER_SCRIPT = "unregister";
		
	private String redisHost = null;
	private int redisPort = -1;		
	private String httpServer = null;
	private String wsServer = null;
	private boolean enabled = false;
	
	@Autowired
	private AppConfiguration configuration;
	
	@Autowired 
	private ApplicationContext applicationContext;
					
	public void setUp() throws Exception {
		final String SERVER_ADDRESS_FORMAT = "%s://%s:%d";
		
		// Configure the redis connection
		setRedis();
		
		if (getRedisHost() == null || getRedisPort() < 0) {
			// Redis is not defined
			return;
		}
		log.debug("Found redis host at " + getRedisHost() + ":" + getRedisPort());
		
		String localAddress = getLocalAddress();
		if (localAddress == null) {
			// Proxy is not defined
			return;
		}		

		int port = getConfiguration().getHttpConfiguration().getPort();
		log.debug("Found local host at " + localAddress + ":" + port);
		
		boolean isSsl = getConfiguration().getHttpConfiguration().isSslConfigured();
		
		final String HTTP_PROTOCOL = "http" + (isSsl?"s":"");
		final String WS_PROTOCOL = "ws" + (isSsl?"s":"");
		
		this.httpServer = String.format(SERVER_ADDRESS_FORMAT, HTTP_PROTOCOL, localAddress, port);
		this.wsServer = String.format(SERVER_ADDRESS_FORMAT, WS_PROTOCOL, localAddress, port);		
		this.enabled = true;
	}
		
	public Jedis initJedis() {
		return new Jedis(getRedisHost(), getRedisPort());
	}
	
	private void setRedis() {
		String redisHost = System.getenv(REDIS_HOST_KEY);
		String redisPort = System.getenv(REDIS_PORT_KEY);
		
		if (!(redisHost == null || redisPort == null)) {
			this.redisHost = redisHost;
			this.redisPort = Integer.parseInt(redisPort);
		}
	}
	
	public String getLocalAddress() throws IOException {
		// Gets the localhost ip by pinging the proxy
		String host = null;
		ProxyConfiguration config = getConfiguration().getProxyConfiguration();
				
		if (config != null) {
			Socket socket = null;
			try {
				log.debug("Checking proxy host at " + config.getHostname() + ":" + config.getPort());
				socket = new Socket(config.getHostname(), config.getPort());
				host = socket.getLocalAddress().getHostAddress();				
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (Exception e) {}
				}
			}
		}
		return host;
	}	
			
	@Override
	public void start() throws Exception {
		setUp();
		if (!isEnabled()) {
			log.warn("Unable to register with the proxy.  Please check your redis and proxy settings");
			return;
		}
		
		Jedis jedis = null;
		
		try {
			jedis = initJedis();
			jedis.connect();
			
			// Get all http resources and map them to the proxy
			final Map<String, Object> resources = getApplicationContext().getBeansWithAnnotation(Resource.class);
			for (final Object resource: resources.values()) {
				registerHTTP(jedis, resource);
			}
			
			// Get all ws resources and map them to the proxy
			final Map<String, Object> websockets = getApplicationContext().getBeansWithAnnotation(WebSocketListener.class);
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
			String result = (String) j.evalsha(sha1, 1, appname, server);			
			log.debug("Mapping " + appname + ": " + server + " -> " + result);
		} else {
			throw new RegistrationException("Registration script not found!");
		}
	}
	
	private void registerHTTP(Jedis j, Object resource) {
		Resource r = resource.getClass().getAnnotation(Resource.class);
		Path p = resource.getClass().getAnnotation(Path.class);
		register(j, r.name(), getHttpServer() + p.value());
	}
	
	private void registerWS(Jedis j, Object websocket) {
		WebSocketListener w = websocket.getClass().getAnnotation(WebSocketListener.class);
		Path p = websocket.getClass().getAnnotation(Path.class);
		register(j, w.name(), getWsServer() + p.value());
	}
		
	@Override
	public void stop() throws Exception {
		Jedis jedis = null;
		
		if (!isEnabled()) return;
		
		try {
			jedis = initJedis();
			jedis.connect();
			
			// Get all http resources and map them to the proxy
			final Map<String, Object> resources = getApplicationContext().getBeansWithAnnotation(Resource.class);
			for (final Object resource: resources.values()) {
				unregisterHTTP(jedis, resource);
			}
			
			// Get all ws resources and map them to the proxy
			final Map<String, Object> websockets = getApplicationContext().getBeansWithAnnotation(WebSocketListener.class);
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
			String result = (String) j.evalsha(sha1, 1, appname, server);
			log.debug("Unmapping " + appname + ": " + server + " -> " + result);
		} else {
			throw new RegistrationException("Unregistration script not found!");
		}
	}
	
	private void unregisterHTTP(Jedis j, Object resource) {
		Resource r = resource.getClass().getAnnotation(Resource.class);
		Path p = resource.getClass().getAnnotation(Path.class);
		unregister(j, "/api/"+r.name(), getHttpServer() + p.value());
	}
	
	private void unregisterWS(Jedis j, Object websocket) {
		WebSocketListener w = websocket.getClass().getAnnotation(WebSocketListener.class);
		Path p = websocket.getClass().getAnnotation(Path.class);
		unregister(j,"/ws/"+w.name(), getWsServer() + p.value());
	}
	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
	public AppConfiguration getConfiguration() {
		return configuration;
	}
		
	public String getRedisHost() {
		return redisHost;
	}
		
	public int getRedisPort() {
		return redisPort;
	}
	
	public String getHttpServer() {
		return httpServer;
	}
	
	public String getWsServer() {
		return wsServer;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
}
