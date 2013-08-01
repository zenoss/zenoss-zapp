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