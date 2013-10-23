package org.zenoss.app;

import org.junit.Test;
import org.junit.Assert;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;


public class TestZenossCredentials {

    @Test
    public void testGetFromGlobalConf() {
        ZenossCredentials creds = ZenossCredentials.getFromGlobalConf();
        Assert.assertNotNull(creds.getUsername());
        Assert.assertNotNull(creds.getPassword());
    }

    @Test
    public void testLoadPropertiesFile() {
        try{
            File globalConf = new File(ClassLoader.getSystemResource("global.conf").toURI());
            Properties props = ZenossCredentials.getPropertiesFromFile(globalConf.toString());
            Assert.assertEquals("MYPASSWORD", props.get("zauth-password"));
            Assert.assertEquals("MYUSER", props.get("zauth-username"));
        }catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testInvalidPropertiesFile() {
        Properties props = ZenossCredentials.getPropertiesFromFile("foo.bar");
        Assert.assertNull(props.get("zauth-password"));
    }
}
