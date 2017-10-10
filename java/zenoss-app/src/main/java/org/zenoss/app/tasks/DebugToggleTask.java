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
import io.dropwizard.servlets.tasks.Task;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

import static ch.qos.logback.classic.Level.DEBUG;

/**
 * Changes the logging level of a named logger. Expects parameters of name "logger" and "level".
 */
public class DebugToggleTask extends Task {


    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DebugToggleTask.class);
    private Level previousLevel;
    private boolean toggledToDebug;

    /**
     * Create a new task.
     */
    public DebugToggleTask() {
        super("debugtoggle");
        this.toggledToDebug = false;
    }

    @Override
    public synchronized void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {

        if (!toggledToDebug) {
            //Set root logger or org.zenoss and com.zenoss or????
            LOGGER.info("Setting root logger to debug");
            Logger logger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            previousLevel = logger.getLevel();
            logger.setLevel(DEBUG);
            this.toggledToDebug = true;
            output.write("Set logs to debug");
        } else {
            //reconfigure dropwizard logging
            Logger logger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            logger.setLevel(previousLevel);
            this.toggledToDebug = false;
            output.write("Set logs to default " + previousLevel.toString());
        }
    }

}
