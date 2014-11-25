
package org.zenoss.dropwizardspring.testclasses;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("test")
@Component
public class TestProfile {

}
