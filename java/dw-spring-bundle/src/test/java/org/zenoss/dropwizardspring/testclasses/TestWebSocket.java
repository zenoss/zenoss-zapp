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

package org.zenoss.dropwizardspring.testclasses;


import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;
import org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener;

import javax.ws.rs.Path;


@Path("/test")
@WebSocketListener(name="test")
public class TestWebSocket {

    @OnMessage
    public void handleMsg(String data, Connection conn){}

}
