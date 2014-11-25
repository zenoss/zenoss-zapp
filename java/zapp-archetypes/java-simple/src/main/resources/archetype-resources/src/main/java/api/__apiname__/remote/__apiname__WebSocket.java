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
