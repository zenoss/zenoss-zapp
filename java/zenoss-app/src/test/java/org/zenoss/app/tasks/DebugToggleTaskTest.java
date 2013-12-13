package org.zenoss.app.tasks;

import ch.qos.logback.classic.Level;
import com.yammer.dropwizard.config.LoggingConfiguration;
import com.yammer.dropwizard.config.LoggingFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DebugToggleTaskTest {

    @Test
    public void testExectue() throws Exception {


        LoggingConfiguration config = new LoggingConfiguration();
        config.setLevel(Level.INFO);
        new LoggingFactory(config, "blam").configure();


        Logger log = LoggerFactory.getLogger("ROOT");
        assertFalse(log.isDebugEnabled());

        DebugToggleTask task = new DebugToggleTask("blam", config);
        StringWriter sw = new StringWriter();
        task.execute(null, new PrintWriter(sw));
        assertEquals("Set logs to debug", sw.toString());
        assertTrue(log.isDebugEnabled());

        sw = new StringWriter();
        task.execute(null, new PrintWriter(sw));
        assertEquals("Set logs to default", sw.toString());
        assertFalse(log.isDebugEnabled());

    }
}
