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

package org.zenoss.dropwizardspring.eventbus;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventBusConfiguration {
    @JsonProperty
    private int maxEventBusThreads = 10;

    @JsonProperty
    private int minEventBusThreads = 5;

    @JsonProperty
    private int broadcastThreadKeepAliveMillis = 60000;

    public int getMaxEventBusThreads() {
        return maxEventBusThreads;
    }

    public void setMaxEventBusThreads(int maxBroadcastThreads) {
        this.maxEventBusThreads = maxBroadcastThreads;
    }

    public int getMinEventBusThreads() {
        return minEventBusThreads;
    }

    public void setMinEventBusThreads(int minBroadcastThreads) {
        this.minEventBusThreads = minBroadcastThreads;
    }

    public int getEventBusThreadKeepAliveMillis() {
        return broadcastThreadKeepAliveMillis;
    }

    public void setEventBusThreadKeepAliveMillis(int broadcastThreadKeepAliveMillis) {
        this.broadcastThreadKeepAliveMillis = broadcastThreadKeepAliveMillis;
    }
}
