package org.zenoss.app.zauthbundle;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Component;

/**
 * Zapp security utility class
 * User: scleveland
 * Date: 4/8/14
 * Time: 10:48 AM
 */

@Component
public class ZappSecurity {

    /** return the thread-local shiro subject */
    public Subject getSubject() {
        return SecurityUtils.getSubject();
    }
}
