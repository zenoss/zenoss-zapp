// Copyright 2014 The Serviced Authors.
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


package org.zenoss.dropwizardspring.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.shiro.subject.Subject;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenoss.dropwizardspring.websockets.annotations.OnClose;
import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final Map<ListenerType, ListenerProxy> listeners;
    private final CloseListenerProxy closeListener;
    private final String path;


    enum ListenerType {STRINGLISTENER, BYTELISTENER}

    public SpringWebSocketServlet(Object listener, ExecutorService executorService, EventBus syncEventBus, EventBus asyncEventBus, String path) {
        Preconditions.checkNotNull(path);
        Preconditions.checkNotNull(executorService);
        Preconditions.checkNotNull(syncEventBus);
        Preconditions.checkNotNull(asyncEventBus);
        this.path = path;
        this.executorService = executorService;
        this.syncEventBus = syncEventBus;
        this.asyncEventBus = asyncEventBus;
        this.listeners = createListenerProxies(listener);
        this.closeListener = createCloseListenerProxy(listener);
    }

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        return new TextBinaryWebSocket(request);
    }

    final class TextBinaryWebSocket implements OnTextMessage, OnBinaryMessage {
        private Connection connection;
        private WebSocketSession session;
        private final HttpServletRequest request;

        TextBinaryWebSocket(HttpServletRequest request) {
            this.request = request;
        }

        @Override
        public void onMessage(String data) {
            ListenerProxy listener = listeners.get(ListenerType.STRINGLISTENER);
            if (null != listener) {
                try {
                    ((TextListenerProxy) listener).onMessage(data, this.session);
                } catch (ClassCastException e) {
                    throw new RuntimeException("No text listeners are provided", e);
                }
            }
        }

        @Override
        public void onMessage(byte[] data, int offset, int length) {
            ListenerProxy listener = listeners.get(ListenerType.BYTELISTENER);
            if (null != listener) {
                final byte[] msgData = Arrays.copyOfRange(data, offset, length + offset);
                try {
                    ((BinaryListenerProxy) listener).onMessage(msgData, this.session);
                } catch (ClassCastException e) {
                    throw new RuntimeException("No binary listeners are provided", e);
                }
            }
        }

        @Override
        public void onOpen(Connection connection) {
            LOGGER.info("onOpen( connection={})", connection);
            this.connection = connection;

            //XXX grab the subject object on connect, the zauth bundle adds the attribute
            //    this needs to be done before onMessage because the attribute disappears
            Subject subject = (Subject) request.getAttribute("zenoss-subject");
            this.session = new WebSocketSession(subject, request, connection);
            syncEventBus.register(this);
            asyncEventBus.register(this);
        }

        @Override
        public void onClose(int closeCode, String message) {
            LOGGER.info("onClose( closeCode={}, message={})", connection, message);
            if (null != closeListener) {
                closeListener.onClose(closeCode, message, this.session);
            }
            syncEventBus.unregister(this);
            asyncEventBus.unregister(this);
        }

        @Subscribe
        @SuppressWarnings({"unused"})
        public void handle(final WebSocketBroadcast.Message event) {
            //either binary or string proxy will work in this case as they are proxying the same object
            ListenerProxy listener = listeners.values().iterator().next();
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

    private CloseListenerProxy createCloseListenerProxy(Object object) {
        Method call = null;
        for (Method m : object.getClass().getMethods()) {
            if (m.getAnnotation(OnClose.class) != null) {
                if (call == null) {
                    call = m;
                } else {
                    throw new IllegalArgumentException("Only one OnClose annotation supported per class: " + object.getClass());
                }
            }
        }
        if (call == null) {
            return null;
        }
        Class<?>[] params = call.getParameterTypes();
        if (params.length == 3 &&
                Integer.class.isAssignableFrom(params[0]) &&
                String.class.isAssignableFrom(params[1]) &&
                WebSocketSession.class.isAssignableFrom(params[2])) {
            LOGGER.info("WebSocket OnClose listener registered on {} using handler {}:{}", path, object.getClass(), call.getName());
            return new CloseListenerProxy(object, call);
        } else {
            throw new IllegalArgumentException("OnClose-annotated method must have signature: (int, String, WebSocketConnection)");
        }
    }

    private Map<ListenerType, ListenerProxy> createListenerProxies(Object object) {
        Map<ListenerType, ListenerProxy> proxies = new HashMap<>(2);
        List<Method> calls = new ArrayList<Method>(2);
        for (Method m : object.getClass().getMethods()) {
            if (m.getAnnotation(OnMessage.class) != null) {
                calls.add(m);
            }
        }
        if (calls.size() == 0) {
            throw new IllegalArgumentException("Object does not have listener method: " + object.getClass());
        }

        if (calls.size() > 2) {
            throw new IllegalArgumentException("Only two OnMessage annotations supported per class: " + object.getClass());
        }


        for (Method call : calls) {
            //identify which proxy method to call
            ListenerProxy proxy = null;
            Class<?> returnClass = call.getReturnType();
            Class<?>[] params = call.getParameterTypes();
            if (params.length == 2) {
                if (WebSocketSession.class.isAssignableFrom(params[1])) {
                    if (String.class.isAssignableFrom(params[0])) {
                        proxy = new StringListenerProxy(object, call);
                        if (proxies.containsKey(ListenerType.STRINGLISTENER)) {
                            throw new IllegalArgumentException("Only one string listener is supported per class: " + object.getClass());
                        }
                        proxies.put(ListenerType.STRINGLISTENER, proxy);
                    } else if (byte[].class.isAssignableFrom(params[0])) {
                        proxy = new BinaryListenerProxy(object, call);
                        if (proxies.containsKey(ListenerType.BYTELISTENER)) {
                            throw new IllegalArgumentException("Only one byte listener is supported per class: " + object.getClass());
                        }
                        proxies.put(ListenerType.BYTELISTENER, proxy);
                    } else if (void.class.equals(returnClass)) {
                        proxy = new JsonListenerProxy(object, call, params[0]);
                        if (proxies.containsKey(ListenerType.STRINGLISTENER)) {
                            throw new IllegalArgumentException("Only one string listener is supported per class: " + object.getClass());
                        }
                        proxies.put(ListenerType.STRINGLISTENER, proxy);

                    } else {
                        proxy = new JsonListenerProxyWithResponse(object, call, params[0], returnClass);
                        if (proxies.containsKey(ListenerType.STRINGLISTENER)) {
                            throw new IllegalArgumentException("Only one string listener is supported per class: " + object.getClass());
                        }
                        proxies.put(ListenerType.STRINGLISTENER, proxy);
                    }
                }
            }
            if (proxy == null) {
                throw new IllegalArgumentException("Object does not have valid listener method: " + object.getClass());
            }
            LOGGER.info("WebSocket Endpoint registered on {} using handler {}:{}", path, object.getClass(), call.getName());
        }
        return proxies;
    }

    private final class CloseListenerProxy {
        final Object obj;
        final Method call;

        CloseListenerProxy(Object listener, Method call) {
            this.obj = listener;
            this.call = call;
        }

        void onClose(int closeCode, String message, WebSocketSession session) {
            try {
                call.invoke(obj, closeCode, message, session);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private abstract class ListenerProxy {
        final Object obj;
        final Method call;

        ListenerProxy(Object listener, Method call) {
            this.obj = listener;
            this.call = call;
        }

        Object invoke(Object data, WebSocketSession session) {
            try {
                return call.invoke(obj, data, session);
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

        abstract void onMessage(String data, WebSocketSession session);
    }

    /**
     * Listener proxy for onMessage methods that accept String.class and Connection.class
     */
    final class StringListenerProxy extends TextListenerProxy {
        StringListenerProxy(Object listener, Method m) {
            super(listener, m);
        }

        void onMessage(String data, WebSocketSession session) {
            invoke(data, session);
        }
    }

    /**
     * Listener proxy for onMessage methods that accept byte[].class and Connection.class
     */
    final class BinaryListenerProxy extends ListenerProxy {
        BinaryListenerProxy(Object listener, Method m) {
            super(listener, m);
        }

        void onMessage(byte[] data, WebSocketSession session) {
            invoke(data, session);
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

        void onMessage(String data, WebSocketSession session) {
            try {
                Object pojo = mapper.readValue(data, pojoClass);
                invoke(pojo, session);
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

        void onMessage(String data, WebSocketSession session) {
            Object pojo;
            try {
                pojo = mapper.readValue(data, pojoClass);
            } catch (IOException ex) {
                LOGGER.error("Exception deserializing data: {} into pojoClass: {}", data, pojoClass);
                LOGGER.error(" with exception", ex);
                throw new RuntimeException(ex);
            }
            Object result = invoke(pojo, session);
            String value;
            try {
                value = mapper.writeValueAsString(result);
            } catch (IOException ex) {
                LOGGER.error("Exception serializing return pojo: {} from pojoClass", result, returnClass);
                LOGGER.error(" with exception", ex);
                throw new RuntimeException(ex);
            }
            try {
                session.sendMessage(value);
            } catch (IOException ex) {
                LOGGER.debug("Exception while sending response: " + ex.getMessage());
                if (!ex.getMessage().contains("Broken pipe")) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
