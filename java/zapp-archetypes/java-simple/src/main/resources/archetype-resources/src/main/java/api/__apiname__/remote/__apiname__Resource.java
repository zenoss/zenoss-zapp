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
