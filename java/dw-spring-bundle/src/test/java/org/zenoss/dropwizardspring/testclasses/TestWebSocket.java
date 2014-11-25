
package org.zenoss.dropwizardspring.testclasses;


import org.zenoss.dropwizardspring.websockets.WebSocketSession;
import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;
import org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;


@Path("/test")
@WebSocketListener(name="test")
public class TestWebSocket {

    @OnMessage
    public void handleMsg(String data, WebSocketSession session) {
    }

}
