package org.zenoss.app.example.api.RandomGenerator.remote;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.zenoss.app.example.api.RandomGenerator.RandomGeneratorAPI;
import org.zenoss.app.example.api.RandomGenerator.RandomResponse;
import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;
import org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener;

import javax.ws.rs.Path;
import java.io.IOException;
import java.util.ArrayList;

@Path("/ws/example")
@WebSocketListener
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


    @OnMessage
    public void echo(String data, Connection connection) throws IOException {

        ArrayList<String> input = mapper.readValue(data, new TypeReference<ArrayList<String>>() {
        });
        connection.sendMessage(mapper.writeValueAsString(input));

    }

}
