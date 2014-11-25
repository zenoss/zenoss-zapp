
package org.zenoss.dropwizardspring.testclasses;


import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.springframework.context.annotation.Profile;
import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;
import org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener;

@Profile("broken")
@WebSocketListener(name="broken")
public class BrokenWebSocket {

    @OnMessage
    public void test(String data, Connection conn){}
}
