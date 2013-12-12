package org.zenoss.app.tasks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.tasks.Task;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

import static ch.qos.logback.classic.Level.toLevel;
import static java.lang.String.format;

/**
 * Changes the logging level of a named logger. Expects parameters of name "logger" and "level".
 */
public class LoggerLevelTask extends Task {


    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LoggerLevelTask.class);

    /**
     * Create a new task.
     */
    public LoggerLevelTask() {
        super("loggerlevel");
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        String loggerName = getLoggerParam(parameters);
        String levelName = getLevelParam(parameters);

        if (loggerName != null) {
            LOGGER.info("Setting level {} on logger {}", levelName, loggerName);
            setLevel(loggerName, levelName);
            output.print(format("Set level %s on logger %s", levelName, loggerName));
        } else {
            LOGGER.info("Setting level {} on logger {}", levelName, loggerName);
            output.print(format("No logger specified"));
        }

    }

    static String getLevelParam(ImmutableMultimap<String, String> parameters) {
        String levelName = parameters.containsKey("level") ? parameters.get("level").iterator().next() : null;
        return levelName == null ? null : levelName.trim();
    }

    static String getLoggerParam(ImmutableMultimap<String, String> parameters) {
        String loggerName = parameters.containsKey("logger") ? parameters.get("logger").iterator().next() : null;
        return loggerName == null ? null : loggerName.trim();
    }

    static final void setLevel(String loggerName, String levelName) {
        Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        if ("null".equalsIgnoreCase(levelName)) {
            logger.setLevel(null);
        } else {
            Level level = toLevel(levelName, null);
            if (level != null) {
                logger.setLevel(level);
            }
        }
    }

}
