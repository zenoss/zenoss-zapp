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


package org.zenoss.app.example.api.RandomGenerator.remote;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.zenoss.app.example.api.RandomGenerator.RandomGeneratorAPI;
import org.zenoss.app.example.api.RandomGenerator.RandomResponse;
import org.zenoss.dropwizardspring.annotations.Resource;

import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;

/**
 * Example REST resource provider
 *
 * @see <a href="http://dropwizard.codahale.com/manual/">http://dropwizard.codahale.com/manual/</a>
 *
 */
@Resource(name="RandomGenerator") //Annotation ensures it is loaded and registered via Spring
@Path("/example")
@Produces(MediaType.APPLICATION_JSON)
public class RandomGeneratorResource {

    @Autowired
    RandomGeneratorAPI api;

    @Path("/rand_int")
    @Timed
    @GET
    public RandomResponse random(@QueryParam("min")Optional<Integer>min, @QueryParam("max")Optional<Integer>max) {
        return api.random(min, max);
    }
}
