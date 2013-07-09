package org.zenoss.dropwizardspring.websockets.events;

/**
 * Broadcast a pojo as json across all websockets connections who match
 * sender.
 */
public class BroadcastWebSocketPojo {

    public BroadcastWebSocketPojo(Class<?> sender, Object pojo) {
        this.sender = sender;
        this.pojo = pojo;
    }

    public Object pojo() {
        return pojo;
    }

    public Class<?> sender() {
        return sender;
    }

    private final Object pojo;
    private final Class<?> sender;
}
