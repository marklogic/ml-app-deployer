package com.marklogic.appdeployer.command.schemas;

import org.junit.Test;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.appdeployer.command.Command;
import com.marklogic.appdeployer.command.databases.DeployContentDatabasesCommand;
import com.marklogic.appdeployer.command.databases.DeploySchemasDatabaseCommand;
import com.marklogic.appdeployer.command.databases.DeployTriggersDatabaseCommand;
import com.marklogic.appdeployer.command.restapis.DeployRestApiServersCommand;

public class LoadSchemasDataTest  extends AbstractAppDeployerTest {

	@Test
	public void testSchemaLoading() {
        initializeAppDeployer(new DeploySchemasDatabaseCommand(), 
        		new DeployTriggersDatabaseCommand(), 
        		new DeployContentDatabasesCommand(1), 
        		new DeployRestApiServersCommand(),
        		newCommand());
        appDeployer.deploy(appConfig);
        
        
        
       //.undeploy(appConfig);
    }

	private Command newCommand() {
		return new LoadSchemasDataCommand();
	}
	
}
