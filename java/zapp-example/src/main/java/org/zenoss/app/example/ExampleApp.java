
package org.zenoss.app.example;


import org.zenoss.app.AutowiredApp;

public class ExampleApp extends AutowiredApp<ExampleAppConfiguration> {

    public static void main(String[] args) throws Exception {
        new ExampleApp().run(args);
    }

    @Override
    public String getAppName() {
        return "Example App";
    }

    @Override
    protected Class<ExampleAppConfiguration> getConfigType() {
        return ExampleAppConfiguration.class;
    }

}
