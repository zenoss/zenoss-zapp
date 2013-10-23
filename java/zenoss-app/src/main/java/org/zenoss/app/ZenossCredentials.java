package org.zenoss.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * A class for storing the credentials necessary to post metrics.
 */
public class ZenossCredentials {
    private static final Logger log = LoggerFactory.getLogger(ZenossCredentials.class);
    // parameters
    private static final String USERNAME = "zauth-username";
    private static final String PASSWORD = "zauth-password";

    // defaults
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "zenoss";


    @JsonProperty
    private String username = null;

    @JsonProperty
    private String password = null;

    public ZenossCredentials() {

    }

    public ZenossCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Static factory method that creates a ZenossCredentials by
     * reading the username and password from global.conf
     * @return ZenossCredentials with the username and password set
     */
    static ZenossCredentials getFromGlobalConf() {
        String globalConf = getZenHome() + "/etc/global.conf";
        Properties props = ZenossCredentials.getPropertiesFromFile(globalConf);
        return new ZenossCredentials(props.getProperty(USERNAME, DEFAULT_USERNAME),
                props.getProperty(PASSWORD, DEFAULT_PASSWORD));
    }

    private static String getZenHome() {
        String zenhome = System.getenv("ZENHOME");
        if (zenhome != null) {
            return zenhome;
        }
        return "/opt/zenoss";
    }

    static Properties getPropertiesFromFile(String fileName) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(fileName));
        } catch(IOException e) {
            log.error("Unable to read properties from " + fileName, e);
        }
        return props;
    }
}
