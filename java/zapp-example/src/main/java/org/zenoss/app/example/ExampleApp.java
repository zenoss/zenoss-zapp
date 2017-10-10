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

    /**
     * Use the following overriden method to specify whether or not
     * to leverage Swagger and its associated UI in this applicaiton.
     *
     * The following is an example of what must be added to the application's
     * configuration file to set up Swagger properly:
     * <p>
     *     swagger:<br>
     *         resourcePackage: org.zenoss.app.example.api.RandomGenerator.remote<br>
     *         title: Example API<br>
     *         version: v1<br>
     *         description: Example API<br>
     *         contact: info@zenoss.com<br>
     *         license: Apache 2.0<br>
     *         licenseUrl: https://www.apache.org/licenses/LICENSE-2.0<br>
     * </p>
     * <p>
     * Once the above is added to the configuration file and this method is enabled
     * the application's resource classes can be decorated with the proper @Api and
     * {@code @ApiOperation} annotations.
     * </p>
     * <ul>
     * <li>For an example of swagger being used, see
     *     <a href="https://github.com/zenoss/dataconsumer/ingest-api">
     *         https://github.com/zenoss/dataconsumer/ingest-api</a></li>
     *
     * <li>For swagger annotation references, see
     *     <a href="https://github.com/swagger-api/swagger-core/wiki/Annotations-1.5.X">
     *         https://github.com/swagger-api/swagger-core/wiki/Annotations-1.5.X</a></li>
     * </ul>
     *
     * @return true if Swagger will be used, false otherwise.
     */
    @Override
    public boolean isLoadSwagger() { return true; }

    @Override
    protected Class<ExampleAppConfiguration> getConfigType() {
        return ExampleAppConfiguration.class;
    }

}
