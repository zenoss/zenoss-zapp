
package ${package};

import com.fasterxml.jackson.annotation.JsonProperty;
import org.zenoss.app.AppConfiguration;
import ${package}.api.${apiname}.configs.${apiname}Configuration;

import javax.validation.Valid;

public class ${appname}Configuration extends AppConfiguration {
    
    @Valid
    @JsonProperty("sub_configuration")
    private ${apiname}Configuration apiConfiguration = new ${apiname}Configuration();
    
    public ${apiname}Configuration get${apiname}Configuration() {
        return apiConfiguration;
    }
}