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
