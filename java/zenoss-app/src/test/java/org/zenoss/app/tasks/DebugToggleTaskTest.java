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

package org.zenoss.app.tasks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class DebugToggleTaskTest {

    @Test
    public void testExectue() throws Exception {

        Logger logger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.WARN);

        assertFalse(logger.isDebugEnabled());

        DebugToggleTask task = new DebugToggleTask("blam");
        StringWriter sw = new StringWriter();
        task.execute(null, new PrintWriter(sw));
        assertEquals("Set logs to debug", sw.toString());
        org.slf4j.Logger log = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        assertTrue(log.isDebugEnabled());

        sw = new StringWriter();
        task.execute(null, new PrintWriter(sw));
        assertEquals("Set logs to default WARN", sw.toString());
        assertFalse(log.isDebugEnabled());

    }
}
