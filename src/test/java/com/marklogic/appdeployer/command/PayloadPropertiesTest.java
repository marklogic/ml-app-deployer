package com.marklogic.appdeployer.command;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.appdeployer.command.databases.DeployContentDatabasesCommand;
import com.marklogic.appdeployer.command.databases.DeployTriggersDatabaseCommand;
import com.marklogic.mgmt.resource.databases.DatabaseManager;
import com.marklogic.rest.util.Fragment;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PayloadPropertiesTest extends AbstractAppDeployerTest {

	@Test
	public void testXML() {
		appConfig.getConfigDir().setBaseDir(new File("src/test/resources/payload-properties-test/xml/ml-config"));
		//Currently DeployContentDatabasesCommand does not support XML configuration files
		//this.test();
	}

	@Test
	public void testJSON() {
		appConfig.getConfigDir().setBaseDir(new File("src/test/resources/payload-properties-test/json/ml-config"));
		this.test();
	}

	private void test() {
		Map<String, String> includes = new HashMap<String, String>();
		appConfig.setExcludeFields(new String[] {"triggers-database"});
		includes.put("triple-index", "false");
		appConfig.setIncludeFields(includes);

		initializeAppDeployer(new DeployContentDatabasesCommand(2));
		appDeployer.deploy(appConfig);

		DatabaseManager dbMgr = new DatabaseManager(this.manageClient);

		appDeployer.deploy(appConfig);

		Fragment db = dbMgr.getPropertiesAsXml(appConfig.getContentDatabaseName());
		assertEquals("false", db.getElementValue("//m:triple-index"));
		assertNull(db.getElementValue("//m:triggers-database"));
	}

	@After
	public void teardown() {
		undeploySampleApp();
	}
}
