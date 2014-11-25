
package org.zenoss.dropwizardspring.testclasses;

import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.tasks.Task;

import java.io.PrintWriter;

@org.zenoss.dropwizardspring.annotations.Task
public class FakeTask extends Task {

    public FakeTask() {
        super("fake task");
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
    }
}
