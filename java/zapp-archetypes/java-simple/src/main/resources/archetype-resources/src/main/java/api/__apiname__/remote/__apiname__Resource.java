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


package ${package}.api.${apiname}.remote;

import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import ${package}.api.${apiname}.${apiname}API;
import ${package}.api.${apiname}.${apiname}Response;
import org.zenoss.dropwizardspring.annotations.Resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * ${appname} REST resource provider
 *
 * @see <a href="http://dropwizard.codahale.com/manual/">http://dropwizard.codahale.com/manual/</a>
 *
 */
@Resource(name="${apiname}") //Annotation ensures it is loaded and registered via Spring
@Path("/${apiurl}")
@Produces(MediaType.APPLICATION_JSON)
public class ${apiname}Resource {
    
    @Autowired
    ${apiname}API api;
    
    /* TODO: Implement methods here */
}
