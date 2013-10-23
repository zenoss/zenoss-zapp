package org.zenoss.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;
import org.junit.Assert;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;


class MyApplicationConfiguration extends AppConfiguration {

}

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

    @Test
    public void testZenossCredentialsYamlFile() {
        InputStream is = ClassLoader.getSystemResourceAsStream("testConfig.yaml");
        ObjectMapper objectMapper = new ObjectMapper( new YAMLFactory());
        try {
            MyApplicationConfiguration config = objectMapper.readValue(is, MyApplicationConfiguration.class);
            ZenossCredentials creds = config.getZenossCredentials();
            Assert.assertEquals("test", creds.getUsername());
            Assert.assertEquals("test", creds.getPassword());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
