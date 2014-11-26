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
