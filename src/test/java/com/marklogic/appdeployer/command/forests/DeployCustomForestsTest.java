package com.marklogic.appdeployer.command.forests;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.appdeployer.ConfigDir;
import com.marklogic.appdeployer.command.databases.DeployContentDatabasesCommand;
import org.junit.After;
import org.junit.Test;

import java.io.File;

/**
 * Created by rrudin on 7/19/2016.
 */
public class DeployCustomForestsTest extends AbstractAppDeployerTest {

	@After
	public void tearDown() {
		//undeploySampleApp();
	}

	@Test
	public void test() {
		appConfig.setConfigDir(new ConfigDir(new File("src/test/resources/sample-app/custom-forests")));
		initializeAppDeployer(new DeployContentDatabasesCommand(1), new DeployCustomForestsCommand());
		deploySampleApp();
	}
}
