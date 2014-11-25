// Copyright 2014 The Serviced Authors.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
    static final String DEFAULT_USERNAME = "admin";
    static final String DEFAULT_PASSWORD = "zenoss";


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


    static class Builder {
        /**
         * Static factory method that creates a ZenossCredentials by
         * reading the username and password from global.conf
         *
         * @return ZenossCredentials with the username and password set
         */
        ZenossCredentials getFromGlobalConf() {
            String globalConf = getZenHome() + "/etc/global.conf";
            Properties props = getPropertiesFromFile(globalConf);
            return new ZenossCredentials(props.getProperty(USERNAME, DEFAULT_USERNAME),
                    props.getProperty(PASSWORD, DEFAULT_PASSWORD));
        }

        String getZenHome() {
            String zenhome = System.getenv("ZENHOME");
            if (zenhome != null) {
                return zenhome;
            }
            return "/opt/zenoss";
        }

        Properties getPropertiesFromFile(String fileName) {
            Properties props = new Properties();
            try {
                props.load(new FileInputStream(fileName));
            } catch (IOException e) {
                log.error("Unable to read properties from " + fileName, e);
            }
            return props;
        }
    }
}
