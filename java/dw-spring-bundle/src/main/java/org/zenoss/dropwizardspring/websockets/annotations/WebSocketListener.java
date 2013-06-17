package org.zenoss.dropwizardspring.websockets.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * This annotation marks a class to be loaded by  spring and registered as a websocket listener.
 */
@Component
@Retention(RetentionPolicy.RUNTIME)
public @interface WebSocketListener {
}
