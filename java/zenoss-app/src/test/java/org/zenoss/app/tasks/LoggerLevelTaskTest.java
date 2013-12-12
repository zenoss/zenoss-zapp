package org.zenoss.app.tasks;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class LoggerLevelTaskTest {


    @Test
    public void testGetLoggerParam() {

        ImmutableMultimap.Builder<String, String> params = ImmutableMultimap.builder();
        params.put("logger", "testVal");
        assertEquals("testVal", LoggerLevelTask.getLoggerParam(params.build()));

        params = ImmutableMultimap.builder();
        params.put("foo", "testVal");
        assertEquals(null, LoggerLevelTask.getLoggerParam(params.build()));


    }

    @Test
    public void testGetLevelParam() {
        ImmutableMultimap.Builder<String, String> params = ImmutableMultimap.builder();
        params.put("level", "testVal");
        assertEquals("testVal", LoggerLevelTask.getLevelParam(params.build()));

        params = ImmutableMultimap.builder();
        params.put("foo", "testVal");
        assertEquals(null, LoggerLevelTask.getLevelParam(params.build()));

    }

    @Test
    public void testSetLevel() {
        Logger log = LoggerFactory.getLogger("org.test");
        boolean debugEnabled = log.isDebugEnabled();
        LoggerLevelTask.setLevel("org.test", debugEnabled ? "info" : "debug");
        assertNotEquals(debugEnabled, log.isDebugEnabled());

    }

    @Test
    public void testExecute() throws Exception {
        ImmutableMultimap.Builder<String, String> params = ImmutableMultimap.builder();

        LoggerLevelTask task = new LoggerLevelTask();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        task.execute(params.build(), pw);
        assertEquals("No logger specified", sw.toString());

        sw = new StringWriter();
        pw = new PrintWriter(sw);

        params = ImmutableMultimap.builder();
        params.put("logger", "testVal");
        params.put("level", "info");
        task.execute(params.build(), pw);
        assertEquals("Set level info on logger testVal", sw.toString());

        sw = new StringWriter();
        pw = new PrintWriter(sw);

        params = ImmutableMultimap.builder();
        params.put("logger", "null");
        params.put("level", "null");
        task.execute(params.build(), pw);
        assertEquals("Set level null on logger null", sw.toString());

    }
}
