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

package org.zenoss.dropwizardspring.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

/**
 * Servlet to accept websocket connections with text based messages.
 */
public final class SpringWebSocketServlet extends WebSocketServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringWebSocketServlet.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService executorService;
    private final EventBus syncEventBus;
    private final EventBus asyncEventBus;
    private final ListenerProxy listener;
    private final String path;

    public SpringWebSocketServlet(Object listener, ExecutorService executorService, EventBus syncEventBus, EventBus asyncEventBus, String path) {
        Preconditions.checkNotNull(path);
        Preconditions.checkNotNull(executorService);
        Preconditions.checkNotNull(syncEventBus);
        Preconditions.checkNotNull(asyncEventBus);
        this.path = path;
        this.executorService = executorService;
        this.syncEventBus = syncEventBus;
        this.asyncEventBus = asyncEventBus;
        this.listener = createListenerProxy(listener);
    }

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        return new TextBinaryWebSocket();
    }

    final class TextBinaryWebSocket implements OnTextMessage, OnBinaryMessage {
        private Connection connection;

        @Override
        public void onMessage(String data) {
            try {
                ((TextListenerProxy) listener).onMessage(data, this.connection);
            } catch (ClassCastException e) {
                throw new RuntimeException("No text listeners are provided", e);
            }
        }

        @Override
        public void onMessage(byte[] data, int offset, int length) {
            final byte[] msgData = Arrays.copyOfRange(data, offset, length + offset);
            try {
                ((BinaryListenerProxy) listener).onMessage(msgData, this.connection);
            } catch (ClassCastException e) {
                throw new RuntimeException("No binary listeners are provided", e);
            }
        }

        @Override
        public void onOpen(Connection connection) {
            LOGGER.info("onOpen( connection={})", connection);
            this.connection = connection;
            syncEventBus.register(this);
            asyncEventBus.register(this);
        }

        @Override
        public void onClose(int closeCode, String message) {
            LOGGER.info("onClose( closeCode={}, message={})", connection, message);
            syncEventBus.unregister(this);
            asyncEventBus.unregister(this);
        }

        @Subscribe
        @SuppressWarnings({"unused"})
        public void handle(final WebSocketBroadcast.Message event) {
            if (listener.webSocketEndPoint() == event.webSocketEndPoint()) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (event.isStringClass()) {
                                String message = event.asString();
                                connection.sendMessage(message);
                            } else {
                                assert event.isByteArrayClass();
                                byte[] message = event.asByteArray();
                                connection.sendMessage(message, 0, message.length);
                            }
                        } catch (IOException e) {
                            LOGGER.error("Failed broadcasting : {}", event);
                            LOGGER.error(" w/exception:", e);
                        }
                    }
                });
            }
        }
    }

    private ListenerProxy createListenerProxy(Object object) {
        Method call = null;
        for (Method m : object.getClass().getMethods()) {
            if (m.getAnnotation(OnMessage.class) != null) {
                call = m;
                break;
            }
        }
        if (call == null) {
            throw new IllegalArgumentException("Object does not have listener method: " + object.getClass());
        }

        //identify which proxy method to call
        ListenerProxy proxy = null;
        Class<?> returnClass = call.getReturnType();
        Class<?>[] params = call.getParameterTypes();
        if (params.length == 2) {
            if (Connection.class.isAssignableFrom(params[1])) {
                if (String.class.isAssignableFrom(params[0])) {
                    proxy = new StringListenerProxy(object, call);
                } else if (byte[].class.isAssignableFrom(params[0])) {
                    proxy = new BinaryListenerProxy(object, call);
                } else if (void.class.equals(returnClass)) {
                    proxy = new JsonListenerProxy(object, call, params[0]);
                } else {
                    proxy = new JsonListenerProxyWithResponse(object, call, params[0], returnClass);
                }
            }
        }
        if (proxy == null) {
            throw new IllegalArgumentException("Object does not have valid listener method: " + object.getClass());
        }

        LOGGER.info("WebSocket Endpoint registered on {} using handler {}:{}", path, object.getClass(), call.getName());
        return proxy;
    }

    private abstract class ListenerProxy {
        final Object obj;
        final Method call;

        ListenerProxy(Object listener, Method call) {
            this.obj = listener;
            this.call = call;
        }

        Object invoke(Object data, Connection connection) {
            try {
                return call.invoke(obj, data, connection);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        Class<?> webSocketEndPoint() {
            return obj.getClass();
        }
    }

    abstract class TextListenerProxy extends ListenerProxy {

        TextListenerProxy(Object listener, Method call) {
            super(listener, call);
        }

        abstract void onMessage(String data, Connection connection);
    }

    /**
     * Listener proxy for onMessage methods that accept String.class and Connection.class
     */
    final class StringListenerProxy extends TextListenerProxy {
        StringListenerProxy(Object listener, Method m) {
            super(listener, m);
        }

        void onMessage(String data, Connection connection) {
            invoke(data, connection);
        }
    }

    /**
     * Listener proxy for onMessage methods that accept byte[].class and Connection.class
     */
    final class BinaryListenerProxy extends ListenerProxy {
        BinaryListenerProxy(Object listener, Method m) {
            super(listener, m);
        }

        void onMessage(byte[] data, Connection connection) {
            invoke(data, connection);
        }
    }

    /**
     * Listener proxy for onMessage methods that accept a Pojo and Connection.class
     */
    final class JsonListenerProxy extends TextListenerProxy {
        final Class<?> pojoClass;

        JsonListenerProxy(Object listener, Method m, Class<?> pojoClass) {
            super(listener, m);
            this.pojoClass = pojoClass;
        }

        void onMessage(String data, Connection connection) {
            try {
                Object pojo = mapper.readValue(data, pojoClass);
                invoke(pojo, connection);
            } catch (IOException ex) {
                LOGGER.error("Exception deserializing data: {} into pojoClass: {}", data, ex);
                LOGGER.error(" with exception", ex);
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Listener proxy for onMessage methods that accept a Pojo and Connection.class and Responds with a Pojo
     */
    final class JsonListenerProxyWithResponse extends TextListenerProxy {
        final Class<?> pojoClass;
        final Class<?> returnClass;

        JsonListenerProxyWithResponse(Object listener, Method m, Class<?> pojoClass, Class<?> returnClass) {
            super(listener, m);
            this.pojoClass = pojoClass;
            this.returnClass = returnClass;
        }

        void onMessage(String data, Connection connection) {
            Object pojo;
            try {
                pojo = mapper.readValue(data, pojoClass);
            } catch (IOException ex) {
                LOGGER.error("Exception deserializing data: {} into pojoClass: {}", data, pojoClass);
                LOGGER.error(" with exception", ex);
                throw new RuntimeException(ex);
            }
            Object result = invoke(pojo, connection);
            try {
                String value = mapper.writeValueAsString(result);
                connection.sendMessage(value);
            } catch (IOException ex) {
                LOGGER.error("Exception serializing return pojo: {} from pojoClass", result, returnClass);
                LOGGER.error(" with exception", ex);
                throw new RuntimeException(ex);
            }
        }
    }
}