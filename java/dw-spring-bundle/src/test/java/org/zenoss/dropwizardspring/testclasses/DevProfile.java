
package org.zenoss.dropwizardspring.testclasses;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
public class DevProfile {

}
