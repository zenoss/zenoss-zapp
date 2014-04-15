package org.zenoss.app.zauthbundle;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Component;

/**
 * Bean to assist grabbing shiro subject.
 */
@Component
public class ZappSecurity {

    /** return the thread-local shiro subject */
    public Subject getSubject() {
        return SecurityUtils.getSubject();
    }
}
