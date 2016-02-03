package com.marklogic.appdeployer.command.cpf;

import org.junit.After;
import org.junit.Test;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.appdeployer.command.databases.DeployContentDatabasesCommand;
import com.marklogic.appdeployer.command.databases.DeploySchemasDatabaseCommand;
import com.marklogic.appdeployer.command.databases.DeployTriggersDatabaseCommand;
import com.marklogic.appdeployer.command.restapis.DeployRestApiServersCommand;
import com.marklogic.mgmt.cpf.CpfConfigManager;
import com.marklogic.mgmt.cpf.DomainManager;
import com.marklogic.mgmt.cpf.PipelineManager;

public class ManageCpfTest extends AbstractAppDeployerTest {

    @After
    public void teardown() {
        undeploySampleApp();
    }

    @Test
    public void test() {
        initializeAppDeployer(new DeployRestApiServersCommand(), new DeployContentDatabasesCommand(),
                new DeploySchemasDatabaseCommand(), new DeployTriggersDatabaseCommand(), new DeployDomainsCommand(),
                new DeployCpfConfigsCommand(), new DeployPipelinesCommand());

        appDeployer.deploy(appConfig);

        String dbName = appConfig.getTriggersDatabaseName();
        assertEquals(2, new DomainManager(manageClient).getAsXml(dbName).getResourceCount());
        assertEquals(1, new CpfConfigManager(manageClient).getAsXml(dbName).getResourceCount());
        assertEquals(1, new PipelineManager(manageClient).getAsXml(dbName).getResourceCount());
    }

}
