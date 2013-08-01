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

package ${package}.api.${apiname}.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import ${package}.api.${apiname}.${apiname}API;
import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;
import org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener;

import javax.ws.rs.Path;
import java.io.IOException;

@Path("/ws/${apiurl}")
@WebSocketListener(name="${apiname}")
public class ${apiname}WebSocket {
    
    @Autowired
    private ${apiname}API api;
    
    private ObjectMapper mapper = new ObjectMapper();
    
    @OnMessage
    public void handleMessage(String data, Connection connection) throws IOException {
        // TODO: Implement method
        mapper.readTree(data);
    }
}
