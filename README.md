zenoss-zapp
===========
Base project for creating zenoss REST applications.

Intro
----
A zapp is a standalone java application that provides REST webservices. A zapp is built on the
[Dropwizard][1] framework and adds integration with [Spring][2] to enable
auto configuration and registration of objects in [Dropwizard][1]. A zapp is deployed and run as a self contained jar
with all dependencies and given an optional configuration file.

There is also an zapp-example that demonstrates how to create a zapp project.

Getting Started
---
To get started you need provide an implementation of `org.zenoss.app.AutowiredApp` and optionally an implementation of
`org.zenoss.app.AppConfiguration` if you webservice needs to define configuration properties.

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

By default the `AutowiredApp` will scan `org.zenoss.app` and it's sub packages for any classes that need to be loaded
via [Spring][2] or registered in [Dropwizard][1].

Registering REST resources
---
The first thing you will probably want to do is provide a REST resource. Here we use [Jersey][3] to implement the rest
resource and the `org.zenoss.dropwizardspring.annotations.Resource` annotation to automatically register the resource in
 [Dropwizard][1].  Note that `Resource` annotations requires a parameter `name=<ApplicationName>` which is used for auto-registering the zapp on a proxy server.

    @Resource(name="ExampleApp") //Annotation ensures it is loaded and registered via Spring
    @Path("/example")
    @Produces(MediaType.APPLICATION_JSON)
    public class ExampleResource {
    
    @Path("/hello")
    @Timed
    @GET
    public String hello(){ return "hello";}
    …

Read the [Jersey][3] [documentation](https://jersey.java.net/nonav/documentation/2.0/index.html) to how to handle
resource requests.

Websockets
---

### Registering a websocket listener
Websocket listeners can be registered automatically using [Spring][2].  Any
class annotated with the
`org.zenoss.dropwizardspring.websocket.annotations.WebSocketListener` will be
registered to listen on the path defined by the `@Path` annotation.
`WebSocketListener` requires a parameter `name=<ApplicationName>` which is
used for auto-registering the zapp on a proxy server.
Additionally the `org.zenoss.dropwizardsrping.annotations.OnMessage`
annotations is needed to define the method that will handle websocket messages.
The OnMessage annotation supports raw data (text or binary) and automatic
marshalling of Java POJOs using Jackson.  Automatic marshalling from JSON to
Java occurs when the annotated method's first argument is neither a String
object nor a byte array.  Additionally, the WebSocket listener will marshall
a Java POJO into JSON.  Return marshalling from Java to JSON occurs when the
annotated method's return type is non-void and the annotated method's first
parameter is neither a String nor a byte array.  See examples below:

#### OnMessage - Raw Text

    import com.fasterxml.jackson.databind.ObjectMapper;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;
    import org.zenoss.dropwizardsrping.websockets.WebSocketSession;
    import org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener;
    import javax.servlet.http.HttpServletRequest;

    import javax.ws.rs.Path;
    import java.io.IOException;

    @Path("/ws/example")
    @WebSocketListener(name="ExampleApp")
    public class ExampleWebSocket {

        private ObjectMapper mapper = new ObjectMapper();

        @OnMessage
        public void echo(String data, WebSocketSession session) throws IOException {
            ArrayList<String> input = mapper.readValue(data, new TypeReference<ArrayList<String>>() {});
            session.sendMessage(mapper.writeValueAsString(input));
        }
    }

#### OnMessage - Raw Binary

    import org.springframework.beans.factory.annotation.Autowired;
    import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;
    import org.zenoss.dropwizardsrping.websockets.WebSocketSession;
    import org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener;
    import javax.servlet.http.HttpServletRequest;

    import javax.ws.rs.Path;
    import java.io.IOException;

    @Path("/ws/example")
    @WebSocketListener(name="ExampleApp")
    public class ExampleWebSocket {

        @OnMessage
        public void echo(byte[] data, WebSocketSession session) throws IOException {
            session.sendMessage(data);
        }
    }

#### OnMessage - Json Marshalling - Json 2 Java

    import com.fasterxml.jackson.databind.ObjectMapper;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;
    import org.zenoss.dropwizardsrping.websockets.WebSocketSession;
    import org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener;
    import javax.servlet.http.HttpServletRequest;

    import javax.ws.rs.Path;
    import java.io.IOException;

    @Path("/ws/example")
    @WebSocketListener(name="ExampleApp")
    public class ExampleWebSocket {
        private ObjectMapper mapper = new ObjectMapper();

        class Pojo {
            private String message;
            public void setMessage(String message) { this.message = message; }
            public String getMessage() { return message;}
            public Pojo(String message) { this.message = message; }
            public Pojo() { }
        }

        @OnMessage
        public void echo(Pojo pojo, WebSocketSession session) throws IOException {
            session.sendMessage(mapper.writeValueAsString(pojo.getMessage()));
        }
    }

#### OnMessage - Json Unmarshalling/Marshalling - Json 2 Java and Java 2 Json

    import com.fasterxml.jackson.databind.ObjectMapper;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;
    import org.zenoss.dropwizardsrping.websockets.WebSocketSession;
    import org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener;
    import javax.servlet.http.HttpServletRequest;

    import javax.ws.rs.Path;
    import java.io.IOException;

    @Path("/ws/example")
    @WebSocketListener(name="ExampleApp")
    public class ExampleWebSocket {
        class Pojo {
            private String message;
            public void setMessage(String message) { this.message = message; }
            public String getMessage() { return message;}
            public Pojo(String message) { this.message = message; }
            public Pojo() { }
        }

        @OnMessage
        public Pojo echo(Pojo pojo, WebSocketSession session) throws IOException {
            return new Pojo( pojo.message);
        }
    }

### WebSocket Message Broadcast
Zapp WebSockets support listener based message broadcasting.  In other words, a
Zapp can broadcast a message to all connections assigned to a WebSocketListener.
Broadcasting supports String, binary, and Json messages.  Message broadcasts is
supported through the [EventBus] (#event-bus-configuration).  See below for
examples.

#### Broadcast String Message

    @Path("/ws/example")
    @WebSocketListener(name="ExampleApp")
    public class ExampleWebSocket {

        @AutoWired
        @Qualifer("zapp::event-bus::async")
        EventBus eventBus

        @OnMessage
        public void broadcast(String message, WebSocketSession session) throws IOException {
            WebSocketBroadcast.Message wsMessage = WebSocketBroadcast.newMessage( ExampleWebSocket.class, message);
            eventBus.post( wsMessage);
        }
    }

#### Broadcast Binary Message

    @Path("/ws/example")
    @WebSocketListener(name="ExampleApp")
    public class ExampleWebSocket {

        @AutoWired
        @Qualifer("zapp::event-bus::async")
        EventBus eventBus

        @OnMessage
        public void broadcast(String message, WebSocketSession session) throws IOException {
            WebSocketBroadcast.Message wsMessage = WebSocketBroadcast.newMessage( ExampleWebSocket.class, new byte[] {...});
            eventBus.post( wsMessage);
        }
    }

#### Broadcast Pojo 

    @Path("/ws/example")
    @WebSocketListener(name="ExampleApp")
    public class ExampleWebSocket {

        @AutoWired
        @Qualifer("zapp::event-bus::async")
        EventBus eventBus

        class Pojo {
            private String message;
            public void setMessage(String message) { this.message = message; }
            public String getMessage() { return message;}
            public Pojo(String message) { this.message = message; }
            public Pojo() { }
        }

        @OnMessage
        public void broadcast(Pojo pojo, WebSocketSession session) throws IOException {
            WebSocketBroadcast.Message wsMessage = WebSocketBroadcast.newMessage( ExampleWebSocket.class, pojo);
            eventBus.post( wsMessage);
        }
    }
    
Registering Dropwizard Bundles
---
The `org.zenoss.app.annotations.Bundle` annotation, along with the interface `org.zenoss.app.autobundle.AutoBundle` can
be utilized to register additional Dropwizard bundles as part of your application.  An example would be the registering
an AssetBundle so that your Zenoss Application(tm) can server static web content such as JavaScript files.

To register additional bundles either create a new class annotated with `@Bundle` or annotate an existing class, such as
your application class. Also ensure that this class implements the AutoBundle interface.  The following example 
illustrates an application class that registers an AssetBundle that requires no additional configuration classes
(indicated by the `Optional.<class>absent()` return value in the `getRequiredConfig` method call.

This example exposes files in the JAR file under the '/api' directory as static content under the URL path `/api`; the
second parameter to the AssetBundle constructor controls the URL path that is exposed.  Dropwizard does not appear to
announce the accessibility of these files as it does the resources after the startup banner, not does it seem to support
indexing of the files if you perform an HTTP GET on the directory.

It should be noted that for each dropwizard bundle you wish to register you will be required to create a new Java class that is annotated with `@Bundle` and implements the `AutoBundle` interface. Also note
that the package for the AutoBundle must start with org.zenoss.app
    package org.zenoss.app.myservice;
    import org.zenoss.app.annotations.Bundle;
    import org.zenoss.app.autobundle.AutoBundle;
    import com.google.common.base.Optional;
    import com.yammer.dropwizard.assets.AssetsBundle;

    @Bundle
    public class MyServiceApp extends
        AutowiredApp<MyServiceAppConfiguration> implements AutoBundle {

        public static final String APP_NAME = "My Service Zapplication";

        public static void main(String[] args) throws Exception {
            new MyServiceApp().run(args);
        }

        @Override
        public String getAppName() {
            return APP_NAME;
        }

        @Override
        protected Class<MyServiceAppConfiguration> getConfigType() {
            return MyServiceAppConfiguration.class;
        }

        @Override
        public com.yammer.dropwizard.Bundle getBundle() {
            return new AssetsBundle("/api/", "/api/");
        }

        @Override
        public Optional<Class> getRequiredConfig() {
            return Optional.<Class> absent();
        }
    }

Registering Dropwizard objects
---
The `org.zenoss.dropwizardspring.annotations` package contains `HealthChecks`,
`Tasks` and "`Managed`" annotations.  These annotations can be used to
automatically register their respective [Dropwizard][1] components.  Read the
[Dropwizard][1] documentation to find out more about the components.

Spring Profiles
---
You can annotate your components that have different implementations based on
running environment with `Profile`. For example a component that runs in
production can be annotated `@Profile("prod")` and a version of the component
that runs in development can be annotated with `@Profile("dev")`. If
a component is annotated with a profile than it will only be loaded if the
profile matches any of the active profiles.

The bundle sets the default active profile to be `prod`. The active profile can
be changed by setting a command line environment.

    java -Dspring.profiles.active=dev

Read more about Spring [Profiles](http://blog.springsource.com/2011/02/14/spring-3-1-m1-introducing-profile/).

Proxy Registration
---
Zapp allows registration of a resource or websocket listener with a centralized proxy.  To configure this auto-registration, set zapp.autoreg.host and zapp.autoreg.port as system variables pointing to the redis database for the proxy.  Then, set the proxy host and port in the proxy section of the Zapp's configuration.yaml. 

<a name="event-bus-configuration"></a>Application Event Handling with Guava EventBus
---
Zapp provides two Guava EventBus spring beans, zapp::event-bus::sync and
zapp::event-bus::async. The zapp::event-bus::sync bean provides a synchronous
event handling system.  The zapp::event-bus::async provides an asynchronous
event handling system.  Use appropriately.  See example autowiring and
configuration below.

### Synchronous EventBus Subscriber - Field based configuration

    import com.google.common.eventbus.EventBus;
    import com.google.common.eventbus.Subscribe;
    
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.stereotype.Component;
    
    @Component
    public class AnEventBusSubscriber
    {
        @Autowired
        @Qualifer("zapp::event-bus::sync")
        EventBus eventBus;
    
        @PostConstruct
        public void registerSubscribers() {
            eventBus.register( this);
        }
    
        @Subscribe public void eventHandler( Object event) {
            //do something with event
        }
    }

### Asynchronous EventBus Subscriber - Constructor based configuration

    import com.google.common.eventbus.EventBus;
    import com.google.common.eventbus.Subscribe;
    
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.stereotype.Component;
    
    @Component
    class AnEventBusSubscriber
    {
        @Autowired
        public AnEventBusSubscriber( @Qualifer("zapp::event-bus::async") EventBus eventBus) {
            this.eventBus = eventBus;
            this.eventBus.register( this);
        }
    
        @Subscribe public void eventHandler( Object event) {
            //do something with event
        }
    }

Read more about Guava [EventBus](http://code.google.com/p/guava-libraries/wiki/EventBusExplained)

Writing unit tests
---
Writing tests for zapp resource requires a combination of spring and dropwizard test classes. Add the dropwizard and
spring dependencies to your pom.

         <dependency>
             <groupId>com.yammer.dropwizard</groupId>
             <artifactId>dropwizard-testing</artifactId>
             <version>${dropwizard.version}</version>
             <scope>test</scope>
         </dependency>
         <dependency>
             <groupId>org.springframework</groupId>
             <artifactId>spring-test</artifactId>
             <version>${spring.version}</version>
             <scope>test</scope>


Extend the Dropwizard test class `ResourceTest` to test resources. If you want Spring to autowire your resources you'll
need to annotate your test class to provide a Spring environment. The annotated static class in the test will allow you
to register mock beans or any other bean needed to run the test.


    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(loader = AnnotationConfigContextLoader.class)
    @ActiveProfiles({"dev","test"}) //Profiles used by the test
    public class QueryTest extends ResourceTest {

        //static configuration class provides the spring configuration i.e. what beans are loaded
        @Configuration
        @ComponentScan(basePackages = {"org.zenoss.app"})//scan for annotated classes
        static class ContextConfiguration {

            @Bean //provide you app config if needed
            public QueryAppConfiguration getQueryAppConfiguration() {
                return new QueryAppConfiguration();
            }
        }

        @Autowired
        PerformanceMetricQueryResources resource;

        @Test
        public void myTest() throws Exception {
            //see http://dropwizard.codahale.com/manual/testing/#testing-resources
        }

        /*
         * (non-Javadoc)
         *
         * @see com.yammer.dropwizard.testing.ResourceTest#setUpResources()
         */
        // @Override
        protected void setUpResources() throws Exception {
            addResource(resource);
        }
    }


Building and running
---
The example zapp contains examples of the mvn build plugins needed to create a zapp jar.  To build example app run the
following in the zapp-example directory:
    
    mvn package

To run the zapp-example run the following, replacing `<version>`:

    java -jar target/zapp-example-<version>.jar server target/etc/configuration.yaml
    

You can also run the example zapp without packaging directly via maven.

    mvn compile exec:java

Once the server runs successfully, it returns a random number between 0 and 10 at /example/rand\_int.

    curl -k "https://localhost:8443/example/rand_int"
    
The server is configured to use the SSL and the port number of 8443 in this case. You can generate a random number in a different range as follows:

    curl -k "https://localhost:8443/example/rand_int?min=0&max=100"

To build your own zapp you can copy and modify the build plugins in the `pom.xml` in the zapp-example project or you can
use the zapp maven archetype to generate a zapp project skeleton.

### Zapp archetype
A skeleton for a zapp project can be created using maven archetypes. To create a project type

    mvn archetype:generate -DarchetypeArtifactId=java-simple -DarchetypeGroupId=org.zenoss.zapp.archetypes

The archetype requires some properties to be entered:

    * `groupId`: The group for you artifact, generally something like `org.zenoss.<group>`
    * `artifactId`: The artifact id, e.g `helloworld-service`
    * `apiname`: name of the API where your business logic is contained e.g. `helloAPI`
    * `apiurl`: url to access API via rest. e.g. `/helloworld`
    * `appname`: : Name of the app `helloapp`
    * `package`:  defaults to `org.zenoss.app.<appname>`.

[1]: http://dropwizard.codahale.com/
[2]: http://www.springsource.org/
[3]: https://jersey.java.net/
