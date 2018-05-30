package com.marklogic.appdeployer.command.es;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.appdeployer.ConfigDir;
import com.marklogic.appdeployer.command.databases.DeployContentDatabasesCommand;
import com.marklogic.appdeployer.command.databases.DeployOtherDatabasesCommand;
import com.marklogic.appdeployer.command.databases.DeploySchemasDatabaseCommand;
import com.marklogic.appdeployer.command.restapis.DeployRestApiServersCommand;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.StringHandle;
import org.junit.After;
import org.junit.Test;

import java.io.File;

public class GenerateModelArtifactsTest extends AbstractAppDeployerTest {

	@After
	public void tearDown() {
		undeploySampleApp();
	}

	@Test
	public void test() {
		String projectPath = "src/test/resources/entity-services-project";
		File srcDir = new File(projectPath, "src");
		if (srcDir.exists()) {
			srcDir.delete();
		}
		appConfig.setConfigDir(new ConfigDir(new File(projectPath + "/src/main/ml-config")));
		appConfig.setModelsPath(projectPath + "/data/entity-services");
		appConfig.getModulePaths().clear();
		appConfig.getModulePaths().add(projectPath + "/src/main/ml-modules");
		appConfig.setSchemasPath(projectPath + "/src/main/ml-schemas");
		appConfig.setModelsDatabase(appConfig.getContentDatabaseName());

		initializeAppDeployer(new DeployContentDatabasesCommand(1), new DeploySchemasDatabaseCommand(),
			new DeployOtherDatabasesCommand(1),
			new DeployRestApiServersCommand(), new GenerateModelArtifactsCommand());
		deploySampleApp();

		assertTrue(new File(projectPath, "src/main/ml-modules/ext/entity-services/Race-0.0.1.xqy").exists());
		assertTrue(new File(projectPath, "src/main/ml-modules/options/Race.xml").exists());
		assertTrue(new File(projectPath, "src/main/ml-schemas/Race-0.0.1.xsd").exists());
		assertTrue(new File(projectPath, "src/main/ml-schemas/tde/Race-0.0.1.tdex").exists());
		assertTrue(new File(projectPath, "src/main/ml-config/databases/content-database.json").exists());
		assertTrue("A schemas db file needs to be created since the ES content-database.json file refers to one",
			new File(projectPath, "src/main/ml-config/databases/schemas-database.json").exists());

		// Verify the model was loaded into the database
		DatabaseClient modelsClient = appConfig.newAppServicesDatabaseClient(appConfig.getModelsDatabase());
		try {
			String raceModel = modelsClient.newDocumentManager().read("/marklogic.com/entity-services/models/race.json").nextContent(new StringHandle()).get();
			assertTrue("Simple smoke test to make sure the race model came back", raceModel.contains("This schema represents a Runner"));
		} finally {
			modelsClient.release();
		}

		deploySampleApp();

		// These shouldn't exist because the content is the same
		assertFalse(new File(projectPath, "src/main/ml-modules/ext/entity-services/Race-0.0.1-GENERATED.xqy").exists());
		assertFalse(new File(projectPath, "src/main/ml-modules/options/Race-GENERATED.xml").exists());
		assertFalse(new File(projectPath, "src/main/ml-config/databases/content-database-GENERATED.json").exists());
		assertFalse(new File(projectPath, "src/main/ml-schemas/Race-0.0.1-GENERATED.xsd").exists());
		assertFalse(new File(projectPath, "src/main/ml-schemas/Race-0.0.1-GENERATED.tdex").exists());
	}
}
