package org.zenoss.dropwizardspring.websockets.events;

/**
 * Broadcast a message across all websocket connections that match sender
 */
public class BroadcastWebSocketMessage {

    public BroadcastWebSocketMessage(Class<?> sender, String message) {
        this.message = message;
        this.sender = sender;
    }

    public String message() {
        return message;
    }

    public Class<?> sender() {
        return sender;
    }

    private final String message;
    private final Class<?> sender;
}