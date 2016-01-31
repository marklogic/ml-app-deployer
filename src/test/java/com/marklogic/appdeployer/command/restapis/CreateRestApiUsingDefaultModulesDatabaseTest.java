package com.marklogic.appdeployer.command.restapis;

import java.io.File;

import org.junit.After;
import org.junit.Test;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.appdeployer.ConfigDir;
import com.marklogic.appdeployer.command.databases.DeployContentDatabasesCommand;
import com.marklogic.mgmt.appservers.ServerManager;
import com.marklogic.rest.util.Fragment;

public class CreateRestApiUsingDefaultModulesDatabaseTest extends AbstractAppDeployerTest {

    @Test
    public void test() {
        /**
         * Use a config directory that only has a content database file and a REST API file.
         */
        appConfig
                .setConfigDir(new ConfigDir(new File("src/test/resources/sample-app/default-modules-database-config")));

        appConfig.setModulesDatabaseName("Modules");

        /**
         * Since we're using the Modules database, and it's used by other OOTB servers, we have to configure our command
         * to not include the modules database when the REST API server is deleted.
         */
        DeployRestApiServersCommand command = new DeployRestApiServersCommand();
        command.setDeleteModulesDatabase(false);

        initializeAppDeployer(new DeployContentDatabasesCommand(1), command);
        appDeployer.deploy(appConfig);

        // Verify that the Modules database is used and then a new modules database wasn't created
        ServerManager mgr = new ServerManager(manageClient, "Default");
        Fragment props = mgr.getPropertiesAsXml("sample-app");
        assertEquals("Modules", props.getElementValue("/m:http-server-properties/m:modules-database"));
        assertEquals("sample-app-content", props.getElementValue("/m:http-server-properties/m:content-database"));
    }

    @After
    public void teardown() {
        undeploySampleApp();
    }
}
