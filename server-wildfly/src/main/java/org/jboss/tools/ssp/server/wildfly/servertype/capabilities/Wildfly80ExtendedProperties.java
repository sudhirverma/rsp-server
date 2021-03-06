/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.ssp.server.wildfly.servertype.capabilities;

import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.wildfly.servertype.launch.IDefaultLaunchArguments;
import org.jboss.tools.ssp.server.wildfly.servertype.launch.Wildfly80DefaultLaunchArguments;

public class Wildfly80ExtendedProperties extends JBossAS710ExtendedProperties {
	public Wildfly80ExtendedProperties(IServer obj) {
		super(obj);
	}
	@Override
	public String getRuntimeTypeVersionString() {
		return "8.x"; //$NON-NLS-1$
	}
	
	@Override
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		return new Wildfly80DefaultLaunchArguments(server);
	}
	
	@Override
	public boolean requiresJDK() {
		return true;
	}
	@Override
	public String getJMXUrl() {
			return getJMXUrl(getManagementPort(), "service:jmx:http-remoting-jmx"); //$NON-NLS-1$
	}
	
	@Override
	public int getManagementPort() {
		return 9990;
	}
	
	@Override
	public boolean allowExplodedModulesInWarLibs() {
		return true;
	}
	
	public boolean allowExplodedModulesInEars() {
		return true;
	}
//	@Override
//	public String getManagerServiceId() {
//		return IJBoss7ManagerService.WILDFLY_VERSION_900;
//	}
//	
//	@Override
//	public IExecutionEnvironment getDefaultExecutionEnvironment() {
//		return JavaRuntime.getExecutionEnvironmentsManager().getEnvironment("JavaSE-1.7"); //$NON-NLS-1$
//	}
//	

}
