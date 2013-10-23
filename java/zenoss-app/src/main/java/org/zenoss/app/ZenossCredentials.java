package org.zenoss.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A class for storing the credentials necessary to post metrics.
 */
public class ZenossCredentials {
    private static final Logger log = LoggerFactory.getLogger(ZenossCredentials.class);
    // parameters
    private static final String USERNAME="zauth-username";
    private static final String PASSWORD="zauth-password";

    // defaults
    static final String DEFAULT_USERNAME="admin";
    static final String DEFAULT_PASSWORD="zenoss";

    private final String username;
    private final String password;

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
        String username = ZenossCredentials.executeGlobalConf(USERNAME);
        String password = ZenossCredentials.executeGlobalConf(PASSWORD);
        return new ZenossCredentials(username, password);
    }

    /**
     * Executes zenglobalconf and returns the value
     * @param param String the name of the parameter we want from global conf
     * @return String either the value from global conf or the default for the parameter
     */
    static String executeGlobalConf(String param) {
        try{
            // this makes the assumption that $ZENHOME/bin/zenglobalconf is on the PATH
            Process p  = Runtime.getRuntime().exec("zenglobalconf -p " + param);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            return in.readLine();
        }
        catch (IOException e) {
            log.error("Error reading " + param + " from global conf", e);
            if (param.equals(USERNAME)) {
                return DEFAULT_USERNAME;
            } else {
                return DEFAULT_PASSWORD;
            }
        }

    }

}
