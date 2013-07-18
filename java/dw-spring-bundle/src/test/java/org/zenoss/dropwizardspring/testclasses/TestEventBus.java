package org.zenoss.dropwizardspring.testclasses;

import com.google.common.eventbus.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TestEventBus {

    @Autowired
    public TestEventBus(@Qualifier("zapp::event-bus::sync") EventBus syncEventBus, @Qualifier("zapp::event-bus::async") EventBus asyncEventBus) {
        this.syncEventBus = syncEventBus;
        this.asyncEventBus = asyncEventBus;
    }

    public EventBus getSyncEventBus() {
        return this.syncEventBus;
    }

    public EventBus getAsynEventBus() {
        return this.asyncEventBus;
    }

    private EventBus syncEventBus;
    private EventBus asyncEventBus;
}
