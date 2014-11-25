
package org.zenoss.dropwizardspring.websockets.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks a method that can accept websocket messages.  Method must have a signature of {@link String},
 * {@link org.eclipse.jetty.websocket.WebSocket.Connection}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnMessage {
}
