package org.zenoss.app.example.api.RandomGenerator.remote;

import java.io.IOException;

import javax.ws.rs.Path;

import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.zenoss.app.example.api.RandomGenerator.RandomGeneratorAPI;
import org.zenoss.app.example.api.RandomGenerator.RandomResponse;
import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;
import org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

@Path("/ws/example")
@WebSocketListener(name="RandomGenerator")
public class RandomGeneratorWebSocket {

    @Autowired
    private RandomGeneratorAPI api;

    private ObjectMapper mapper = new ObjectMapper();

    @OnMessage
    public void handleMessage(String data, Connection connection) throws IOException {

        RandomRequest request = mapper.readValue(data, RandomRequest.class);

        RandomResponse x = api.random(Optional.fromNullable(request.getMin()), Optional.fromNullable(request.getMax()));

        connection.sendMessage(mapper.writeValueAsString(x));

    }

}
