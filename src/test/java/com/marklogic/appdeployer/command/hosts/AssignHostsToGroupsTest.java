package com.marklogic.appdeployer.command.hosts;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.appdeployer.ConfigDir;
import com.marklogic.appdeployer.command.groups.DeployGroupsCommand;
import com.marklogic.mgmt.resource.hosts.HostManager;

public class AssignHostsToGroupsTest extends AbstractAppDeployerTest {

	@Test
	public void assignSingleHostToExistingGroup() {
		HostManager mgr = new HostManager(this.manageClient);
		appConfig.setConfigDir(new ConfigDir(new File("src/test/resources/sample-app/other-group")));

		final String otherGroup = "sample-app-other-group";
		final String hostname = manageClient.getManageConfig().getHost();

		appConfig.setGroupName("Default");
		Map<String, String> hostGroups = new HashMap<String, String>();
		hostGroups.put(hostname, otherGroup);
		appConfig.setHostGroups(hostGroups);

		initializeAppDeployer(new DeployGroupsCommand(), new AssignHostsToGroupsCommand());

		try {
			deploySampleApp();
		} finally {
			// Undeploy does not work yet.
			undeploySampleApp();
		}
	}
}
