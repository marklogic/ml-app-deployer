package com.marklogic.appdeployer.command.forests;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.appdeployer.command.databases.DeployContentDatabasesCommand;
import com.marklogic.mgmt.ManageClient;
import com.marklogic.mgmt.cma.ConfigurationManager;
import com.marklogic.mgmt.resource.clusters.ClusterManager;
import com.marklogic.mgmt.resource.forests.ForestManager;
import org.junit.Test;

import java.io.File;

public class DeployForestsViaCmaTest extends AbstractAppDeployerTest {

	/**
	 * We don't really know that the forests were created via CMA, as if the ML cluster isn't 9.0-5, the call to
	 * /manage/v3 should fail silently and switch over to the forests endpoint for creating forests. The intent of this
	 * test then is just to have a focused test on creating a handful of forests with inspection of logging done manually.
	 */
	@Test
	public void test() {
		appConfig.setOptimizeWithCma(true);
		appConfig.getFirstConfigDir().setBaseDir(new File("src/test/resources/sample-app/db-only-config"));

		initializeAppDeployer(new DeployContentDatabasesCommand(6));

		ForestManager mgr = new ForestManager(manageClient);

		try {
			deploySampleApp();
			for (int i = 1; i <= 6; i++) {
				assertTrue(mgr.exists(appConfig.getContentDatabaseName() + "-" + i));
			}
		} finally {
			undeploySampleApp();
			for (int i = 1; i <= 6; i++) {
				assertFalse(mgr.exists(appConfig.getContentDatabaseName() + "-" + i));
			}
		}
	}
}
