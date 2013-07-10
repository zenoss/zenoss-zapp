/*
 * ****************************************************************************
 *
 *  Copyright (C) Zenoss, Inc. 2013, all rights reserved.
 *
 *  This content is made available according to terms specified in
 *  License.zenoss under the directory where your Zenoss product is installed.
 *
 * ***************************************************************************
 */

package org.zenoss.app.example.api.RandomGenerator.remote;

import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.zenoss.app.example.api.RandomGenerator.RandomGeneratorAPI;
import org.zenoss.app.example.api.RandomGenerator.RandomResponse;
import org.zenoss.dropwizardspring.annotations.Resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Example REST resource provider
 *
 * @see <a href="http://dropwizard.codahale.com/manual/">http://dropwizard.codahale.com/manual/</a>
 *
 */
@Resource("RandomGenerator") //Annotation ensures it is loaded and registered via Spring
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
