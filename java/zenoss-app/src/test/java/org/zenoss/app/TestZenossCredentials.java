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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Properties;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


class MyApplicationConfiguration {
    @JsonProperty
    private ZenossCredentials zenossCredentials;

    public ZenossCredentials getZenossCredentials() {
        return zenossCredentials;
    }

    public void setZenossCredentials(ZenossCredentials zenossCredentials) {
        this.zenossCredentials = zenossCredentials;
    }
}

public class TestZenossCredentials {

    @Test
    public void testGetFromGlobalConf() throws Exception {
        ZenossCredentials creds = new ZenossCredentials.Builder().getFromGlobalConf();
        Assert.assertNotNull(creds.getUsername());
        Assert.assertNotNull(creds.getPassword());
    }

    @Test
    public void testInvalidPropertiesFile() throws Exception {
        Properties props = new ZenossCredentials.Builder().getPropertiesFromFile("foo.bar");
        Assert.assertNull(props.get("zauth-password"));
    }

    @Test
    public void testZenossCredentialsYamlFile() throws Exception {
        InputStream is = ClassLoader.getSystemResourceAsStream("testConfig.yaml");
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        MyApplicationConfiguration config = objectMapper.readValue(is, MyApplicationConfiguration.class);
        ZenossCredentials creds = config.getZenossCredentials();
        Assert.assertEquals("myusername", creds.getUsername());
        Assert.assertEquals("mypassword", creds.getPassword());
    }


    @Test
    public void testDefaults() throws Exception {
        ZenossCredentials.Builder b = spy(new ZenossCredentials.Builder());
        when(b.getPropertiesFromFile(anyString())).thenReturn(new Properties());

        ZenossCredentials creds = b.getFromGlobalConf();
        Assert.assertEquals(ZenossCredentials.DEFAULT_USERNAME, creds.getUsername());
        Assert.assertEquals(ZenossCredentials.DEFAULT_PASSWORD, creds.getPassword());
    }
}
