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

import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.junit.Test;
import org.zenoss.dropwizardspring.websockets.SpringWebSocketServlet.TextWebSocket;
import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;
import org.junit.Assert;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class SpringWebSocketServletTest {


    @Test(expected = IllegalArgumentException.class)
    public void testPlainObject() {
        new SpringWebSocketServlet(new Object(), "/test");
    }

    @Test()
    public void testConstructor() {
        new SpringWebSocketServlet(new StringHandler(), "/test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListenerWrongSignature() {
        new SpringWebSocketServlet(new InvalidSignatureHandler(), "/test");
    }


    @Test
    public void testDoWebSocketConnect() {
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(new StringHandler(), "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextWebSocket ws = (TextWebSocket) servlet.doWebSocketConnect(request, "");
//        Assert.notNull(ws);
    }

    @Test
    public void testTextWebSocket() {
        StringHandler handler = new StringHandler();
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextWebSocket tws = (TextWebSocket) servlet.doWebSocketConnect(request, "");
        tws.onOpen(mock(Connection.class));
        tws.onClose(1, "closed");

        String msg = "here is some data";
        tws.onMessage(msg);
        Assert.assertEquals(msg, handler.data);
    }

    @Test
    public void testTextWebSocketJsonInputHandling() {
        JsonHandler handler = new JsonHandler();
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextWebSocket tws = (TextWebSocket) servlet.doWebSocketConnect(request, "");
        tws.onOpen(mock(Connection.class));
        tws.onClose(1, "closed");

        String msg = "{\"message\":\"here is some data\"}";
        Assert.assertEquals(null, handler.data);
        tws.onMessage(msg);
        Assert.assertEquals("here is some data", handler.data.message);
    }

    @Test(expected=RuntimeException.class)
    public void testTextWebSocketJsonInputHandlingWithDeserializeException() throws IOException {
        Connection connection = mock(Connection.class);
        JsonHandlerWithInputError handler = new JsonHandlerWithInputError();
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextWebSocket tws = (TextWebSocket) servlet.doWebSocketConnect(request, "");
        tws.onOpen(mock(Connection.class));
        tws.onMessage("{}");
        verify(connection, never()).sendMessage(anyString());
    }

    @Test
    public void testTextWebSocketJsonInputOutputHandling() throws IOException {
        JsonInputOutputHandler handler = new JsonInputOutputHandler();
        Connection connection = mock(Connection.class);
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextWebSocket tws = (TextWebSocket) servlet.doWebSocketConnect(request, "");

        tws.onOpen(connection);
        String msg = "{\"message\":\"here is some data\"}";
        Assert.assertEquals(null, handler.data);
        tws.onMessage(msg);
        Assert.assertEquals("here is some data", handler.data.message);
        verify( connection).sendMessage( "{\"message\":null}");
    }

    @Test
    public void testTextWebSocketJsonInputHandlingWithSerializeException() throws IOException {
        JsonInputOutputHandlerWithOutputError handler = new JsonInputOutputHandlerWithOutputError();
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextWebSocket tws = (TextWebSocket) servlet.doWebSocketConnect(request, "");
        Connection connection = mock(Connection.class);
        tws.onOpen(connection);

        assertNull( handler.data);
        try {
            tws.onMessage("{\"message\":\"hi\"}");
            fail();
        } catch (RuntimeException ex) {
        }
        assertEquals("hi", handler.data.message);
        verify(connection, never()).sendMessage(anyString());
    }

    public static class Pojo {
        String message;
        public Pojo() {
        }

        public void setMessage(String message) {
            this.message= message;
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
            throw new IOException( "Failure!");
        }
    }

    public static class StringHandler {
        String data;

        @OnMessage
        public void handle(String data, Connection c) {
            this.data = data;
        }
    }

    public static class JsonHandler {
        Pojo data;

        @OnMessage
        public void handle(Pojo data, Connection c) {
            this.data = data;
        }
    }

    public static class JsonHandlerWithInputError {

        @OnMessage
        public void handle(InvalidPojo data, Connection c) {
        }
    }

    public static class JsonInputOutputHandler {
        Pojo data;

        @OnMessage
        public Pojo handle(Pojo data, Connection c) {
            this.data = data;
            return new Pojo();
        }
    }

    public static class JsonInputOutputHandlerWithOutputError {
        Pojo data;

        @OnMessage
        public InvalidPojo handle(Pojo data, Connection c) {
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
