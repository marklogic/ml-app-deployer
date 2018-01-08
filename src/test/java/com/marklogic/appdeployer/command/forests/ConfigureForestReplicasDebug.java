package com.marklogic.appdeployer.command.forests;

import com.marklogic.appdeployer.AppConfig;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.databases.DeployDatabaseCommand;
import com.marklogic.mgmt.ManageClient;
import com.marklogic.mgmt.ManageConfig;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Not an actual test, as this depends on an environment with multiple hosts, which is normally not the case on a
 * development machine.
 */
public class ConfigureForestReplicasDebug {

	public static void main(String[] args) {
		final String host = "localhost"; //args[0];
		final String password = "admin"; //args[1];

		final String dbName = "testdb";

		ManageConfig config = new ManageConfig(host, 8002, "admin", password);
		ManageClient manageClient = new ManageClient(config);
		AppConfig appConfig = new AppConfig();

		appConfig.setDatabaseNamesAndReplicaCounts(dbName + ",1");

		Map<String, Set<String>> databaseHosts = new LinkedHashMap<>();
		Set<String> hosts = new LinkedHashSet<>();
		databaseHosts.put(dbName, hosts);
		appConfig.setDatabaseHosts(databaseHosts);

		Map<String, Set<String>> databaseGroups = new LinkedHashMap<>();
		Set<String> groups = new LinkedHashSet<>();
		groups.add("Default");
		databaseGroups.put(dbName, groups);
		appConfig.setDatabaseGroups(databaseGroups);

		CommandContext context = new CommandContext(appConfig, manageClient, null);

		DeployDatabaseCommand ddc = new DeployDatabaseCommand();
		ddc.setForestsPerHost(1);
		ddc.setCreateDatabaseWithoutFile(true);
		ddc.setDatabaseName(dbName);

		ConfigureForestReplicasCommand cfrc = new ConfigureForestReplicasCommand();

		// Deploy the database, and then configure replicas
		//ddc.execute(context);
		//cfrc.execute(context);

		// Deploy again to make sure there are no errors
		//cfrc.execute(context);

		// Then delete the replicas, and then undeploy the database
		cfrc.undo(context);
		ddc.undo(context);
	}
}
