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