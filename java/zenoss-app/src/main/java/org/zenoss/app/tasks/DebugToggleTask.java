package org.zenoss.app.tasks;

import ch.qos.logback.classic.Logger;
import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.config.LoggingConfiguration;
import com.yammer.dropwizard.config.LoggingFactory;
import com.yammer.dropwizard.tasks.Task;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

import static ch.qos.logback.classic.Level.DEBUG;

/**
 * Changes the logging level of a named logger. Expects parameters of name "logger" and "level".
 */
public class DebugToggleTask extends Task {


    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DebugToggleTask.class);
    private final String bootstrapName;
    private final LoggingConfiguration config;
    private boolean toggledToDebug;

    /**
     * Create a new task.
     */
    public DebugToggleTask(String bootstrapName, LoggingConfiguration config) {
        super("debugtoggle");
        this.bootstrapName = bootstrapName;
        this.config = config;
        this.toggledToDebug = false;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {

        if (!toggledToDebug) {
            //Set root logger or org.zenoss and com.zenoss or????
            LOGGER.info("Setting root logger to debug");
            Logger logger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            logger.setLevel(DEBUG);
            this.toggledToDebug = true;
            output.write("Set logs to debug");
        } else {
            //reconfigure dropwizard logging
            new LoggingFactory(config, bootstrapName).configure();
            this.toggledToDebug = false;
            output.write("Set logs to default");
        }
    }

}
