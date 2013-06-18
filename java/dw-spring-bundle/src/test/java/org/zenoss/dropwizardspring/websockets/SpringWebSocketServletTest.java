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

import static org.mockito.Mockito.mock;

public class SpringWebSocketServletTest {


    @Test(expected = IllegalArgumentException.class)
    public void testPlainObject(){
        new SpringWebSocketServlet(new Object(), "/test");
    }

    @Test()
    public void testConstructor(){
        new SpringWebSocketServlet(new Handler(), "/test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListenerWrongSignature(){
        new SpringWebSocketServlet(new Handler2(), "/test");
    }


    @Test
    public void testDoWebSocketConnect(){
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(new Handler(), "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextWebSocket ws = (TextWebSocket) servlet.doWebSocketConnect(request, "");
//        Assert.notNull(ws);
    }

    @Test
    public void testTextWebSocket(){
        Handler handler = new Handler();
        SpringWebSocketServlet servlet = new SpringWebSocketServlet(handler, "/test");
        HttpServletRequest request = mock(HttpServletRequest.class);
        TextWebSocket tws = (TextWebSocket) servlet.doWebSocketConnect(request, "");
        tws.onOpen(mock(Connection.class));
        tws.onClose(1, "closed");

        String msg = "here is some data";
        tws.onMessage(msg);
        Assert.assertEquals(msg, handler.data);
    }





    public static class Handler{
        String data;
        @OnMessage
        public void handle(String data, Connection c){
            this.data = data;
        }
    }

    public static class Handler2{
        @OnMessage
        public void handle(String data){}
    }



}
