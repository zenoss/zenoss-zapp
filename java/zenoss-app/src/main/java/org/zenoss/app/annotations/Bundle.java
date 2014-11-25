
package org.zenoss.app.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark classes that should be loaded in dropwizard as bundles. Classes with this type will be loaded and registered as
 * bundles in Dropwizard. Requires a public default constructor.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bundle{
}
