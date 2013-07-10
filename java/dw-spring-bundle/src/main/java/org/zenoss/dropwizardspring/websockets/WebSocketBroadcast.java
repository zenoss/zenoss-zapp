package org.zenoss.dropwizardspring.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

/**
 * WebSocketBroadcast creates messages for broadcasting across websockets.  Two kinds of messages are supports
 * String and binary (byte[]).  This class also supports creating json messages.
 */
public final class WebSocketBroadcast {

    /** Create a string broadcast message*/
    public static Message newMessage(Class<?> webSocketEndPoint, String message) {
        return new Message(webSocketEndPoint, message);
    }

    /** Create a binary broadcast message*/
    public static Message newMessage(Class<?> webSocketEndPoint, byte[] message) {
        return new Message(webSocketEndPoint, message);
    }

    /** Create a json message using pojo */
    public static Message newMessage(Class<?> webSocketEndPoint, Object pojo) throws JsonProcessingException {
        String message = mapper.writeValueAsString(pojo);
        return newMessage(webSocketEndPoint, message);
    }

    public static final class Message {
        private Message(Class<?> webSocketEndPoint, Object message) {
            Preconditions.checkNotNull(webSocketEndPoint);
            Preconditions.checkNotNull(message);
            this.message = message;
            this.webSocketEndPoint = webSocketEndPoint;
        }

        public boolean isStringClass() {
            return String.class == message.getClass();
        }

        public boolean isByteArrayClass() {
            return byte[].class == message.getClass();
        }

        public String asString() {
            return message.toString();
        }

        public byte[] asByteArray() {
            return (byte[]) message;
        }

        public Class<?> webSocketEndPoint() {
            return webSocketEndPoint;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "message=" + message +
                    ", webSocketEndPoint=" + webSocketEndPoint +
                    '}';
        }

        private final Object message;
        private final Class<?> webSocketEndPoint;
    }

    private WebSocketBroadcast() {
    }

    private static final ObjectMapper mapper = new ObjectMapper();
}
