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