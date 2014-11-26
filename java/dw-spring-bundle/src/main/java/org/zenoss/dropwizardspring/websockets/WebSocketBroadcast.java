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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Message message1 = (Message) o;
            if (!message.equals(message1.message)) return false;
            if (!webSocketEndPoint.equals(message1.webSocketEndPoint)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int result = message.hashCode();
            result = 31 * result + webSocketEndPoint.hashCode();
            return result;
        }

        private final Object message;
        private final Class<?> webSocketEndPoint;
    }

    private WebSocketBroadcast() {
    }

    private static final ObjectMapper mapper = new ObjectMapper();
}
