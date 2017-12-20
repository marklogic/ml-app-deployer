package com.marklogic.appdeployer.command;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.appdeployer.command.databases.DeployContentDatabasesCommand;
import com.marklogic.mgmt.resource.databases.DatabaseManager;
import com.marklogic.rest.util.Fragment;
import org.junit.After;
import org.junit.Test;

import java.io.File;

public class PayloadPropertiesTest extends AbstractAppDeployerTest {

	@After
	public void teardown() {
		undeploySampleApp();
	}

	@Test
	public void testExcludeProperties() {
		appConfig.getConfigDir().setBaseDir(new File("src/test/resources/payload-properties-test/json/ml-config"));
		appConfig.setExcludeProperties(new String[] {"triggers-database"});

		initializeAppDeployer(new DeployContentDatabasesCommand(2));
		appDeployer.deploy(appConfig);

		DatabaseManager dbMgr = new DatabaseManager(this.manageClient);

		appDeployer.deploy(appConfig);

		Fragment db = dbMgr.getPropertiesAsXml(appConfig.getContentDatabaseName());
		assertEquals("false", db.getElementValue("//m:triple-index"));
		assertNull(db.getElementValue("//m:triggers-database"));
	}

	@Test
	public void testIncludeProperties() {
		appConfig.getConfigDir().setBaseDir(new File("src/test/resources/payload-properties-test/json/ml-config"));
		appConfig.setExcludeProperties(new String[] {"triple-index"});

		initializeAppDeployer(new DeployContentDatabasesCommand(2));
		appDeployer.deploy(appConfig);

		DatabaseManager dbMgr = new DatabaseManager(this.manageClient);
		Fragment db = dbMgr.getPropertiesAsXml(appConfig.getContentDatabaseName());

		assertEquals("true", db.getElementValue("//m:triple-index"));
		assertEquals("Triggers", db.getElementValue("//m:triggers-database"));

		appConfig.setExcludeProperties(null);
		appConfig.setIncludeProperties(new String[] {"triple-index", "database-name"});
		appDeployer.deploy(appConfig);

		db = dbMgr.getPropertiesAsXml(appConfig.getContentDatabaseName());
		assertEquals("false", db.getElementValue("//m:triple-index"));
	}

	@Test
	public void testException() {
		appConfig.getConfigDir().setBaseDir(new File("src/test/resources/payload-properties-test/json/ml-config"));
		appConfig.setExcludeProperties(new String[]{"triple-index"});
		try {
			appConfig.setIncludeProperties(new String[]{"triggers-database"});
		} catch (RuntimeException e) {
			assertNotNull(e);
		}
	}

}
