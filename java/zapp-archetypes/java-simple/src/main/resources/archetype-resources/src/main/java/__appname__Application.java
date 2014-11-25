
package ${package};

import org.zenoss.app.AutowiredApp;

public class ${appname}Application extends AutowiredApp<${appname}Configuration> {
    
    public static void main(String[] args) throws Exception {
        new ${appname}Application().run(args);
    }
    
    @Override
    public String getAppName() {
        return "${appname} App";
    }
    
    @Override
    protected Class<${appname}Configuration> getConfigType() {
        return ${appname}Configuration.class;
    }
    
}