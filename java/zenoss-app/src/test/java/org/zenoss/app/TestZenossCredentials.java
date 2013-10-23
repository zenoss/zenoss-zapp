package org.zenoss.app;

import org.junit.Test;
import org.junit.Assert;


public class TestZenossCredentials {

    @Test
    public void testGetFromGlobalConf() {
        ZenossCredentials creds = ZenossCredentials.getFromGlobalConf();
        Assert.assertNotNull(creds.getUsername());
        Assert.assertNotNull(creds.getPassword());
    }

    @Test
    public void testExecuteGlobalConf() {
        String nullVal = ZenossCredentials.executeGlobalConf("ObviouslyInvalidParameter");
        Assert.assertNull(nullVal);
    }
}
