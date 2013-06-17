package org.zenoss.dropwizardspring.websockets;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Servlet to accept websocket connections with text based messages.
 *
 */
public final class SpringWebSocketServlet extends WebSocketServlet {

    public SpringWebSocketServlet(Object listener) {
        this.listener = new ListenerProxy(listener);
    }

    private final ListenerProxy listener;


    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        return new TextWebSocket();
    }

    private final class TextWebSocket implements OnTextMessage {
        private Connection connection;

        @Override
        public void onMessage(String data) {
            listener.onMessage(data, this.connection);
        }

        @Override
        public void onOpen(Connection connection) {
            //TODO log
            this.connection = connection;
        }

        @Override
        public void onClose(int closeCode, String message) {
            //TODO log
            this.connection = null;
        }
    }

    private final class ListenerProxy {

        private final Object obj;
        private Method call;


        public ListenerProxy(Object listener) {
            this.obj = listener;
            for (Method m : this.obj.getClass().getMethods()) {
                if (m.getAnnotation(OnMessage.class) != null) {
                    this.checkSignature(m);
                    this.call = m;
                    break;
                }
            }
            if (this.call == null) {
                throw new IllegalArgumentException("Object does not have listener method: " + this.obj.getClass());
            }
        }

        private void checkSignature(Method m) {
            Class<?>[] params = m.getParameterTypes();
            if (params.length != 2 || !String.class.isAssignableFrom(params[0]) || !Connection.class.isAssignableFrom(params[1])) {
                throw new IllegalArgumentException("Wrong signature for @OnMessage method: " + m.getName()
                        + " on class " + this.obj.getClass().getName());
            }
        }

        public void onMessage(String data, Connection connection) {
            try {
                this.call.invoke(this.obj, data, connection);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
