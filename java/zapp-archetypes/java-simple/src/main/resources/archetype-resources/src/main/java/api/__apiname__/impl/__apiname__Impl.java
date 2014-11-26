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


package ${package}.api.${apiname}.impl;

import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.zenoss.app.annotations.API;
import ${package}.api.${apiname}.${apiname}API;
import ${package}.${appname}Configuration;
import ${package}.api.${apiname}.${apiname}Response;

/**
 * ${appname} API provider class.
 */
@API //API annotation ensures it is loaded via Spring
public class ${apiname}Impl implements ${apiname}API {
    
    @Autowired
    ${appname}Configuration config;
    
    /* TODO: Add implementation methods */
}
