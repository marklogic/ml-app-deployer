package com.marklogic.appdeployer.export;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.appdeployer.command.cpf.DeployCpfConfigsCommand;
import com.marklogic.appdeployer.command.cpf.DeployDomainsCommand;
import com.marklogic.appdeployer.command.cpf.DeployPipelinesCommand;
import com.marklogic.appdeployer.command.databases.DeployOtherDatabasesCommand;
import com.marklogic.appdeployer.command.restapis.DeployRestApiServersCommand;
import com.marklogic.mgmt.selector.PropertiesResourceSelector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ExportCpfTest extends AbstractExportTest {

	@AfterEach
	public void teardown() {
		undeploySampleApp();
	}

	@Test
	public void exportViaProps() {
		// Need a modules database, so a REST API is deployed
		initializeAppDeployer(new DeployRestApiServersCommand(),
			new DeployOtherDatabasesCommand(1), new DeployDomainsCommand(),
			new DeployCpfConfigsCommand(), new DeployPipelinesCommand());

		appDeployer.deploy(appConfig);

		Properties props = new Properties();
		props.setProperty("cpfConfigs", "sample-app-domain-1");
		props.setProperty("domains", "sample-app-domain-1,sample-app-domain-2");
		props.setProperty("pipelines", "sample-app-pipeline-1");

		ExportedResources resources = new Exporter(manageClient)
			.withTriggersDatabase(appConfig.getTriggersDatabaseName())
			.select(new PropertiesResourceSelector(props))
			.export(exportDir);

		verifyExportedFiles(resources);
	}

	private void verifyExportedFiles(ExportedResources resources) {
		assertEquals(resources.getMessages().get(0),
			"Each exported CPF pipeline has the 'pipeline-id' field removed from it, as that field should be generated by MarkLogic.");

		assertEquals(4, resources.getFiles().size(), "Expected 1 CPF config, 2 domains, and 1 pipeline to be exported");
		assertTrue(new File(exportDir, "cpf/cpf-configs/sample-app-domain-1.json").exists());
		assertTrue(new File(exportDir, "cpf/domains/sample-app-domain-1.json").exists());
		assertTrue(new File(exportDir, "cpf/domains/sample-app-domain-2.json").exists());

		File pipelineFile = new File(exportDir, "cpf/pipelines/sample-app-pipeline-1.json");
		assertTrue(pipelineFile.exists());

		try {
			JsonNode json = objectMapper.readTree(pipelineFile);
			assertEquals("sample-app-pipeline-1", json.get("pipeline-name").asText());
			assertNull(json.get("pipeline-id"),
				"The pipeline-id should be removed when a pipeline is exported, as it's expected to be generated by MarkLogic");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
