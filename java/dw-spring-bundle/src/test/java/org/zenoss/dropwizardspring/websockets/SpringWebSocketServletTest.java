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

import com.google.common.eventbus.EventBus;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.zenoss.dropwizardspring.websockets.SpringWebSocketServlet.TextBinaryWebSocket;
import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class SpringWebSocketServletTest {

    EventBus syncEventBus;
    EventBus asyncEventBus;
    ExecutorService executorService;

    @Before
    public void setUp() {
        syncEventBus = mock(EventBus.class);
        asyncEventBus = mock(EventBus.class);
        executorService = mock(ExecutorService.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPlainObject() {
        new SpringWebSocketServlet(new Object(), executorService, syncEventBus, asyncEventBus, "/test");
    }

    @Test()
    public void testConstructor() {
        new SpringWebSocketServlet(new StringHandler(), executorService, syncEventBus, asyncEventBus, "/test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListenerWrongSignature() {
        new SpringWebSocketServlet(new InvalidSignatureHandler(), executorService, syncEventBus, asyncEventBus, "/test");
    }

    @Test
    public void testDoWebSocketConnect() {
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(new StringHandler(), executorService, syncEventBus, asyncEventBus, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextBinaryWebSocket ws = (TextBinaryWebSocket) servlet.doWebSocketConnect(request, "");
//        Assert.notNull(ws);
    }

    @Test
    public void testTextBinaryWebSocket() {
        StringHandler handler = new StringHandler();
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, executorService, syncEventBus, asyncEventBus, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextBinaryWebSocket tws = (TextBinaryWebSocket) servlet.doWebSocketConnect(request, "");
        tws.onOpen(mock(Connection.class));
        tws.onClose(1, "closed");

        String msg = "here is some data";
        tws.onMessage(msg);
        Assert.assertEquals(msg, handler.data);
    }

    @Test
    public void testBinaryWebSocket() {
        BinaryHandler handler = new BinaryHandler();
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, executorService, syncEventBus, asyncEventBus, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextBinaryWebSocket bws = (TextBinaryWebSocket) servlet.doWebSocketConnect(request, "");
        bws.onOpen(mock(Connection.class));
        bws.onClose(1, "closed");

        byte[] msg = "here is some data".getBytes();
        bws.onMessage(msg, 0, msg.length);
        Assert.assertEquals(new String(msg), new String(handler.data));
    }

    @Test
    public void testTextBinaryWebSocketJsonInputHandling() {
        JsonHandler handler = new JsonHandler();
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, executorService, syncEventBus, asyncEventBus, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextBinaryWebSocket tws = (TextBinaryWebSocket) servlet.doWebSocketConnect(request, "");
        tws.onOpen(mock(Connection.class));
        tws.onClose(1, "closed");

        String msg = "{\"message\":\"here is some data\"}";
        Assert.assertEquals(null, handler.data);
        tws.onMessage(msg);
        Assert.assertEquals("here is some data", handler.data.message);
    }

    @Test(expected = RuntimeException.class)
    public void testTextBinaryWebSocketJsonInputHandlingWithDeserializeException() throws IOException {
        Connection connection = mock(Connection.class);
        JsonHandlerWithInputError handler = new JsonHandlerWithInputError();
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, executorService, syncEventBus, asyncEventBus, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextBinaryWebSocket tws = (TextBinaryWebSocket) servlet.doWebSocketConnect(request, "");
        tws.onOpen(mock(Connection.class));
        tws.onMessage("{}");
        verify(connection, never()).sendMessage(anyString());
    }

    @Test
    public void testTextBinaryWebSocketJsonInputOutputHandling() throws IOException {
        JsonInputOutputHandler handler = new JsonInputOutputHandler();
        Connection connection = mock(Connection.class);
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, executorService, syncEventBus, asyncEventBus, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextBinaryWebSocket tws = (TextBinaryWebSocket) servlet.doWebSocketConnect(request, "");

        tws.onOpen(connection);
        String msg = "{\"message\":\"here is some data\"}";
        Assert.assertEquals(null, handler.data);
        tws.onMessage(msg);
        Assert.assertEquals("here is some data", handler.data.message);
        verify(connection).sendMessage("{\"message\":null}");
    }

    @Test
    public void testTextBinaryWebSocketJsonInputHandlingWithSerializeException() throws IOException {
        JsonInputOutputHandlerWithOutputError handler = new JsonInputOutputHandlerWithOutputError();
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, executorService, syncEventBus, asyncEventBus, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextBinaryWebSocket tws = (TextBinaryWebSocket) servlet.doWebSocketConnect(request, "");
        Connection connection = mock(Connection.class);
        tws.onOpen(connection);

        assertNull(handler.data);
        try {
            tws.onMessage("{\"message\":\"hi\"}");
            fail();
        } catch (RuntimeException ex) {
        }
        assertEquals("hi", handler.data.message);
        verify(connection, never()).sendMessage(anyString());
    }


    @Test
    public void testEventBusRegisterAndUnregister() {
        StringHandler handler = new StringHandler();
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, executorService, syncEventBus, asyncEventBus, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextBinaryWebSocket tws = (TextBinaryWebSocket) servlet.doWebSocketConnect(request, "");
        Connection connection = mock(Connection.class);

        tws.onOpen(connection);
        verify(syncEventBus, times(1)).register(tws);
        verify(asyncEventBus, times(1)).register(tws);

        tws.onClose(0, "a reason");
        verify(syncEventBus, times(1)).unregister(tws);
        verify(asyncEventBus, times(1)).unregister(tws);
    }

    @Test
    public void testBroadcastMessageEvent() throws IOException {
        syncEventBus = new EventBus();
        StringHandler handler = new StringHandler();
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, executorService, syncEventBus, asyncEventBus, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextBinaryWebSocket tws = (TextBinaryWebSocket) servlet.doWebSocketConnect(request, "");
        Connection connection = mock(Connection.class);
        tws.onOpen(connection);

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            }
        }).when(executorService).execute(any(Runnable.class));

        syncEventBus.post(WebSocketBroadcast.newMessage(String.class, "a websocket message"));
        syncEventBus.post(WebSocketBroadcast.newMessage(StringHandler.class, "a websocket message"));
        verify(connection, times(1)).sendMessage("a websocket message");
    }

    @Test
    public void testBroadcastPojoEvent() throws IOException {
        syncEventBus = new EventBus();
        JsonHandler handler = new JsonHandler();
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, executorService, syncEventBus, asyncEventBus, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextBinaryWebSocket tws = (TextBinaryWebSocket) servlet.doWebSocketConnect(request, "");
        Connection connection = mock(Connection.class);
        tws.onOpen(connection);

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            }
        }).when(executorService).execute(any(Runnable.class));

        syncEventBus.post(WebSocketBroadcast.newMessage(Pojo.class, new Pojo("a websocket message")));
        syncEventBus.post(WebSocketBroadcast.newMessage(JsonHandler.class, new Pojo("a websocket message")));
        verify(connection, times(1)).sendMessage("{\"message\":\"a websocket message\"}");
    }

    @Test
    public void testBroadcastBinaryEvent() throws IOException {
        syncEventBus = new EventBus();
        JsonHandler handler = new JsonHandler();
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, executorService, syncEventBus, asyncEventBus, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextBinaryWebSocket tws = (TextBinaryWebSocket) servlet.doWebSocketConnect(request, "");
        Connection connection = mock(Connection.class);
        tws.onOpen(connection);

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            }
        }).when(executorService).execute(any(Runnable.class));

        byte[] message = "a websocket message".getBytes();
        syncEventBus.post(WebSocketBroadcast.newMessage(Pojo.class, message));
        syncEventBus.post(WebSocketBroadcast.newMessage(JsonHandler.class, message));
        verify(connection, times(1)).sendMessage(message, 0 , message.length);
    }

    public static class Pojo {
        String message;

        public Pojo() {
        }


        public Pojo(String message) {
            this.message = message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class InvalidPojo {
        int i;

        public InvalidPojo(int i) {
            this.i = i;
        }

        int getI() throws IOException {
            throw new IOException("Failure!");
        }
    }

    public static class StringHandler {
        String data;

        @OnMessage
        public void handle(String data, WebSocketSession session) {
            this.data = data;
        }
    }

    public static class BinaryHandler {
        byte[] data;

        @OnMessage
        public void handle(byte[] data, WebSocketSession session) {
            this.data = data;
        }
    }

    public static class JsonHandler {
        Pojo data;

        @OnMessage
        public void handle(Pojo data, WebSocketSession session) {
            this.data = data;
        }
    }

    public static class JsonHandlerWithInputError {

        @OnMessage
        public void handle(InvalidPojo data, WebSocketSession session) {
        }
    }

    public static class JsonInputOutputHandler {
        Pojo data;

        @OnMessage
        public Pojo handle(Pojo data, WebSocketSession session) {
            this.data = data;
            return new Pojo();
        }
    }

    public static class JsonInputOutputHandlerWithOutputError {
        Pojo data;

        @OnMessage
        public InvalidPojo handle(Pojo data, WebSocketSession session) {
            this.data = data;
            return new InvalidPojo(1);
        }
    }

    public static class InvalidSignatureHandler {
        @OnMessage
        public void handle(String data) {
        }
    }
}
