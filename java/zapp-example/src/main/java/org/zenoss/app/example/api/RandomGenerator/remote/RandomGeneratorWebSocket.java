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

package org.zenoss.app.example.api.RandomGenerator.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zenoss.app.example.api.RandomGenerator.RandomGeneratorAPI;
import org.zenoss.app.example.api.RandomGenerator.RandomResponse;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@Component("exampleWS")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@ServerEndpoint("/ws/random")
public class RandomGeneratorWebSocket {

    @Autowired
    private RandomGeneratorAPI api;

    private ObjectMapper mapper = new ObjectMapper();

    @OnMessage
    public void handleTextMessage(String data, Session session) throws Exception {
        RandomRequest request = mapper.readValue(data, RandomRequest.class);

        RandomResponse x = api.random(Optional.fromNullable(request.getMin()), Optional.fromNullable(request.getMax()));

        session.getBasicRemote().sendText(mapper.writeValueAsString(x));

    }

}
