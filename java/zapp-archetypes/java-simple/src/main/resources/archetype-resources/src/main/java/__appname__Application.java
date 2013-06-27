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