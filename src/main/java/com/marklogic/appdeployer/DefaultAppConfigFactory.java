package com.marklogic.appdeployer;

import com.marklogic.client.ext.SecurityContextType;
import com.marklogic.mgmt.util.PropertySource;
import com.marklogic.mgmt.util.PropertySourceFactory;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public class DefaultAppConfigFactory extends PropertySourceFactory implements AppConfigFactory {

	public DefaultAppConfigFactory() {
		super();
		initialize();
	}

	public DefaultAppConfigFactory(PropertySource propertySource) {
		super(propertySource);
		initialize();
	}

	private Map<String, BiConsumer<AppConfig, String>> propertyConsumerMap;

	/**
	 * Registers all of the property handlers.
	 */
	public void initialize() {
		// Order matters, so a LinkedHashMap is used to preserve the order
		propertyConsumerMap = new LinkedHashMap<>();

		propertyConsumerMap.put("mlCatchDeployExceptions", (config, prop) -> {
			logger.info("Catch deploy exceptions: " + prop);
			config.setCatchDeployExceptions(Boolean.parseBoolean(prop));
		});

		propertyConsumerMap.put("mlCatchUndeployExceptions", (config, prop) -> {
			logger.info("Catch undeploy exceptions: " + prop);
			config.setCatchUndeployExceptions(Boolean.parseBoolean(prop));
		});

		propertyConsumerMap.put("mlOptimizeWithCma", (config, prop) -> {
			logger.info("Optimize with the Configuration Management API (CMA): " + prop);
			config.setOptimizeWithCma(Boolean.parseBoolean(prop));
		});

		/**
		 * The application name is used as a prefix for default names for a variety of resources, such as REST API servers
		 * and databases.
		 */
		propertyConsumerMap.put("mlAppName", (config, prop) -> {
			logger.info("App name: " + prop);
			config.setName(prop);
		});

		/**
		 * The path to the directory containing all the resource configuration files. Defaults to src/main/ml-config.
		 * mlConfigPath is the preferred one, as its name is consistent with other properties that refer to a path.
		 * mlConfigDir is deprecated but still supported.
		 *
		 * As of 3.3.0, mlConfigPaths is the preferred property, and mlConfigDir and mlConfigPath will be ignored if
		 * it's set.
		 */
		propertyConsumerMap.put("mlConfigPaths", (config, prop) -> {
			logger.info("Config paths: " + prop);
			List<ConfigDir> list = new ArrayList<>();
			for (String path : prop.split(",")) {
				list.add(new ConfigDir(new File(path)));
			}
			config.setConfigDirs(list);
		});
		// TODO Only process if mlConfigPaths not set?
		propertyConsumerMap.put("mlConfigDir", (config, prop) -> {
			logger.info("mlConfigDir is deprecated; please use mlConfigPath; Config dir: " + prop);
			config.setConfigDir(new ConfigDir(new File(prop)));
		});
		propertyConsumerMap.put("mlConfigPath", (config, prop) -> {
			logger.info("Config path: " + prop);
			config.setConfigDir(new ConfigDir(new File(prop)));
		});

		/**
		 * Defines the MarkLogic host that requests should be sent to. Defaults to localhost.
		 */
		propertyConsumerMap.put("mlHost", (config, prop) -> {
			logger.info("App host: " + prop);
			config.setHost(prop);
		});

		/**
		 * Defaults to port 8000. In rare cases, the ML App-Services app server will have been changed to listen on a
		 * different port, in which case you can set this to that port.
		 */
		propertyConsumerMap.put("mlAppServicesPort", (config, prop) -> {
			logger.info("App services port: " + prop);
			config.setAppServicesPort(Integer.parseInt(prop));
		});
		/**
		 * The username and password for a ML user with the rest-admin role that is used for e.g. loading
		 * non-REST API modules via the App Services client REST API, which is defined by the appServicesPort.
		 */
		propertyConsumerMap.put("mlAppServicesUsername", (config, prop) -> {
			logger.info("App Services username: " + prop);
			config.setAppServicesUsername(prop);
		});
		propertyConsumerMap.put("mlAppServicesPassword", (config, prop) -> {
			config.setAppServicesPassword(prop);
		});
		propertyConsumerMap.put("mlAppServicesAuthentication", (config, prop) -> {
			logger.info("App Services authentication: " + prop);
			config.setAppServicesSecurityContextType(SecurityContextType.valueOf(prop.toUpperCase()));
		});
		propertyConsumerMap.put("mlAppServicesCertFile", (config, prop) -> {
			logger.info("App Services cert file: " + prop);
			config.setAppServicesCertFile(prop);
		});
		propertyConsumerMap.put("mlAppServicesCertPassword", (config, prop) -> {
			config.setAppServicesCertPassword(prop);
		});
		propertyConsumerMap.put("mlAppServicesExternalName", (config, prop) -> {
			logger.info("App Services external name: " + prop);
			config.setAppServicesExternalName(prop);
		});
		propertyConsumerMap.put("mlAppServicesSimpleSsl", (config, prop) -> {
			if ("true".equals(prop)) {
				logger.info("Using simple SSL context and 'ANY' hostname verifier for authenticating against the App-Services server");
				config.setAppServicesSimpleSslConfig();
			}
		});

		/**
		 * Set this to true to prevent creating a REST API server by default.
		 */
		propertyConsumerMap.put("mlNoRestServer", (config, prop) -> {
			logger.info("Not creating REST server if no REST config file is found");
			config.setNoRestServer(true);
		});

		/**
		 * If a REST API server is created, it will use the following port. Modules will also be loaded via this port.
		 */
		propertyConsumerMap.put("mlRestPort", (config, prop) -> {
			logger.info("App REST port: " + prop);
			config.setRestPort(Integer.parseInt(prop));
		});
		/**
		 * The username and password for a ML user with the rest-admin role. This user is used for operations against the
		 * Client REST API - namely, loading REST API modules such as options, services, and transforms.
		 */
		propertyConsumerMap.put("mlRestAdminUsername", (config, prop) -> {
			logger.info("REST admin username: " + prop);
			config.setRestAdminUsername(prop);
			if (!propertyExists("mlAppServicesUsername")) {
				logger.info("App Services username: " + prop);
				config.setAppServicesUsername(prop);
			}
		});
		propertyConsumerMap.put("mlRestAdminPassword", (config, prop) -> {
			config.setRestAdminPassword(prop);
			if (!propertyExists("mlAppServicesPassword")) {
				config.setAppServicesPassword(prop);
			}
		});
		propertyConsumerMap.put("mlRestAuthentication", (config, prop) -> {
			logger.info("App REST authentication: " + prop);
			config.setRestSecurityContextType(SecurityContextType.valueOf(prop.toUpperCase()));
		});
		propertyConsumerMap.put("mlRestCertFile", (config, prop) -> {
			logger.info("REST cert file: " + prop);
			config.setRestCertFile(prop);
		});
		propertyConsumerMap.put("mlRestCertPassword", (config, prop) -> {
			logger.info("REST cert password: " + prop);
			config.setRestCertPassword(prop);
		});
		propertyConsumerMap.put("mlRestExternalName", (config, prop) -> {
			logger.info("REST external name: " + prop);
			config.setRestExternalName(prop);
		});

		/**
		 * When modules are loaded via the Client REST API, if the app server requires an SSL connection, then
		 * setting this property will force the simplest SSL connection to be created.
		 */
		propertyConsumerMap.put("mlSimpleSsl", (config, prop) -> {
			if ("true".equals(prop)) {
				logger.info(
					"Using simple SSL context and 'ANY' hostname verifier for authenticating against client REST API server");
				config.setSimpleSslConfig();
			}
		});

		/**
		 * mlUsername and mlPassword are the default username/password for connecting to the app's REST server (if one
		 * exists) and to App-Services on 8000. These are processed before the other username/password properties so that
		 * the other ones will override what these set.
		 */
		propertyConsumerMap.put("mlUsername", (config, prop) -> {
			if (!propertyExists("mlRestAdminUsername")) {
				logger.info("REST admin username: " + prop);
				config.setRestAdminUsername(prop);
			}
			if (!propertyExists("mlAppServicesUsername")) {
				logger.info("App Services username: " + prop);
				config.setAppServicesUsername(prop);
			}
		});

		propertyConsumerMap.put("mlPassword", (config, prop) -> {
			if (!propertyExists("mlRestAdminPassword")) {
				config.setRestAdminPassword(prop);
			}
			if (!propertyExists("mlAppServicesPassword")) {
				config.setAppServicesPassword(prop);
			}
		});


		propertyConsumerMap.put("mlRestServerName", (config, prop) -> {
			logger.info("REST server name: " + prop);
			config.setRestServerName(prop);
		});

		/**
		 * If a test REST API server is created, it will use the following port.
		 */
		propertyConsumerMap.put("mlTestRestPort", (config, prop) -> {
			logger.info("App test REST port: " + prop);
			config.setTestRestPort(Integer.parseInt(prop));
		});

		propertyConsumerMap.put("mlTestRestServerName", (config, prop) -> {
			logger.info("Test REST server name: " + prop);
			config.setTestRestServerName(prop);
		});

		/**
		 * Defines the path to files that should be loaded into a schemas database. Defaults to src/main/ml-schemas.
		 */
		propertyConsumerMap.put("mlSchemasPath", (config, prop) -> {
			logger.info("Schemas path: " + prop);
			config.setSchemasPath(prop);
		});

		propertyConsumerMap.put("mlSchemasDatabaseName", (config, prop) -> {
			logger.info("Schemas database name: " + prop);
			config.setSchemasDatabaseName(prop);
		});

		propertyConsumerMap.put("mlTriggersDatabaseName", (config, prop) -> {
			logger.info("Triggers database name: " + prop);
			config.setTriggersDatabaseName(prop);
		});

		propertyConsumerMap.put("mlCpfDatabaseName", (config, prop) -> {
			logger.info("CPF database name: " + prop);
			config.setCpfDatabaseName(prop);
		});

		propertyConsumerMap.put("mlContentForestsPerHost", (config, prop) -> {
			logger.info("Content forests per host: " + prop);
			config.setContentForestsPerHost(Integer.parseInt(prop));
		});

		propertyConsumerMap.put("mlCreateForests", (config, prop) -> {
			logger.info("Create forests for each deployed database: " + prop);
			config.setCreateForests(Boolean.parseBoolean(prop));
		});

		/**
		 * For any database besides the content database, configure the number of forests per host.
		 */
		propertyConsumerMap.put("mlForestsPerHost", (config, prop) -> {
			logger.info("Forests per host: " + prop);
			String[] tokens = prop.split(",");
			for (int i = 0; i < tokens.length; i += 2) {
				config.getForestCounts().put(tokens[i], Integer.parseInt(tokens[i + 1]));
			}
		});

		/**
		 * This property can specify a comma-delimited list of database names and replica counts as a simple way of
		 * setting up forest replicas - e.g. Documents,1,Security,2.
		 */
		propertyConsumerMap.put("mlDatabaseNamesAndReplicaCounts", (config, prop) -> {
			logger.info("Database names and replica counts: " + prop);
			String[] tokens = prop.split(",");
			Map<String, Integer> map = new HashMap<>();
			for (int i = 0; i < tokens.length; i += 2) {
				map.put(tokens[i], Integer.parseInt(tokens[i + 1]));
			}
			config.setDatabaseNamesAndReplicaCounts(map);
		});

		propertyConsumerMap.put("mlDatabasesWithForestsOnOneHost", (config, prop) -> {
			logger.info("Databases that will have their forest(s) created on a single host: " + prop);
			String[] names = prop.split(",");
			Set<String> set = new HashSet<>();
			for (String name : names) {
				set.add(name);
			}
			config.setDatabasesWithForestsOnOneHost(set);
		});

		propertyConsumerMap.put("mlDatabaseGroups", (config, prop) -> {
			logger.info("Databases and the groups containing the hosts that their forests will be created on: " + prop);
			config.setDatabaseGroups(buildMapOfListsFromDelimitedString(prop));
		});

		propertyConsumerMap.put("mlHostGroups", (config, prop) -> {
			logger.info("Hosts will be assigned to groups: " + prop);
			config.setHostGroups(buildMapFromCommaDelimitedString(prop));
		});

		propertyConsumerMap.put("mlDatabaseHosts", (config, prop) -> {
			logger.info("Databases and the hosts that their forests will be created on: " + prop);
			config.setDatabaseHosts(buildMapOfListsFromDelimitedString(prop));
		});

		propertyConsumerMap.put("mlForestDataDirectory", (config, prop) -> {
			logger.info("Default forest data directory for all databases: " + prop);
			config.setForestDataDirectory(prop);
		});

		propertyConsumerMap.put("mlForestFastDataDirectory", (config, prop) -> {
			logger.info("Default forest fast data directory for all databases: " + prop);
			config.setForestFastDataDirectory(prop);
		});

		propertyConsumerMap.put("mlForestLargeDataDirectory", (config, prop) -> {
			logger.info("Default forest large data directory for all databases: " + prop);
			config.setForestLargeDataDirectory(prop);
		});

		propertyConsumerMap.put("mlReplicaForestDataDirectory", (config, prop) -> {
			logger.info("Default replica forest data directory for all databases: " + prop);
			config.setReplicaForestDataDirectory(prop);
		});

		propertyConsumerMap.put("mlReplicaForestLargeDataDirectory", (config, prop) -> {
			logger.info("Default replica forest large data directory for all databases: " + prop);
			config.setReplicaForestLargeDataDirectory(prop);
		});

		propertyConsumerMap.put("mlReplicaForestFastDataDirectory", (config, prop) -> {
			logger.info("Default replica forest fast data directory for all databases: " + prop);
			config.setReplicaForestFastDataDirectory(prop);
		});

		propertyConsumerMap.put("mlDatabaseDataDirectories", (config, prop) -> {
			logger.info("Databases and forest data directories: " + prop);
			config.setDatabaseDataDirectories(buildMapOfListsFromDelimitedString(prop));
		});

		propertyConsumerMap.put("mlDatabaseFastDataDirectories", (config, prop) -> {
			logger.info("Databases and forest fast data directories: " + prop);
			config.setDatabaseFastDataDirectories(buildMapFromCommaDelimitedString(prop));
		});

		propertyConsumerMap.put("mlDatabaseLargeDataDirectories", (config, prop) -> {
			logger.info("Databases and forest large data directories: " + prop);
			config.setDatabaseLargeDataDirectories(buildMapFromCommaDelimitedString(prop));
		});

		propertyConsumerMap.put("mlDatabaseReplicaDataDirectories", (config, prop) -> {
			logger.info("Databases and replica forest data directories: " + prop);
			config.setDatabaseReplicaDataDirectories(buildMapFromCommaDelimitedString(prop));
		});

		propertyConsumerMap.put("mlDatabaseReplicaFastDataDirectories", (config, prop) -> {
			logger.info("Databases and replica forest fast data directories: " + prop);
			config.setDatabaseReplicaFastDataDirectories(buildMapFromCommaDelimitedString(prop));
		});

		propertyConsumerMap.put("mlDatabaseReplicaLargeDataDirectories", (config, prop) -> {
			logger.info("Databases and replica forest large data directories: " + prop);
			config.setDatabaseReplicaLargeDataDirectories(buildMapFromCommaDelimitedString(prop));
		});

		/**
		 * When undo is invoked on DeployDatabaseCommand (such as via mlUndeploy in ml-gradle), this controls whether
		 * or not forests are deleted, or just their configuration is deleted. If mlDeleteReplicas is set to true, this
		 * has no impact - currently, the forests and their replicas will be deleted for efficiency reasons (results in
		 * fewer calls to the Management REST API.
		 */
		propertyConsumerMap.put("mlDeleteForests", (config, prop) -> {
			logger.info("Delete forests when a database is deleted: " + prop);
			config.setDeleteForests(Boolean.parseBoolean(prop));
		});

		/**
		 * When undo is invoked on DeployDatabaseCommand (such as via mlUndeploy in ml-gradle), this controls whether
		 * primary forests and their replicas are deleted first. Most of the time, you want this set to true
		 * (the default) as otherwise, the database can't be deleted and the Management REST API will throw an error.
		 */
		propertyConsumerMap.put("mlDeleteReplicas", (config, prop) -> {
			logger.info("Delete replicas when a database is deleted: " + prop);
			config.setDeleteReplicas(Boolean.parseBoolean(prop));
		});

		/**
		 * When a REST API server is created, the content database name will default to mlAppName-content. This property
		 * can be used to override that name.
		 */
		propertyConsumerMap.put("mlContentDatabaseName", (config, prop) -> {
			logger.info("Content database name: " + prop);
			config.setContentDatabaseName(prop);
		});

		/**
		 * When a REST API server is created, the modules database name will default to mlAppName-modules. This property
		 * can be used to override that name.
		 */
		propertyConsumerMap.put("mlModulesDatabaseName", (config, prop) -> {
			logger.info("Modules database name: " + prop);
			config.setModulesDatabaseName(prop);
		});

		/**
		 * Specifies the path for flexrep configuration files; used by DeployFlexrepCommand.
		 */
		propertyConsumerMap.put("mlFlexrepPath", (config, prop) -> {
			logger.info("Flexrep path: " + prop);
			config.setFlexrepPath(prop);
		});

		/**
		 * "Default" is the assumed group for group-specific resources, such as app servers and scheduled tasks. This
		 * property can be set to override that.
		 */
		propertyConsumerMap.put("mlGroupName", (config, prop) -> {
			logger.info("Group name: " + prop);
			config.setGroupName(prop);
		});

		/**
		 * When modules are loaded via the Client REST API, this property can specify a comma-delimited set of role/capability
		 * permissions - e.g. rest-reader,read,rest-writer,update.
		 */
		propertyConsumerMap.put("mlModulePermissions", (config, prop) -> {
			logger.info("Module permissions: " + prop);
			config.setModulePermissions(prop);
		});

		/**
		 * When modules are loaded via the Client REST API, this property can specify a comma-delimited set of extensions
		 * for files that should be loaded as binaries.
		 */
		propertyConsumerMap.put("mlAdditionalBinaryExtensions", (config, prop) -> {
			String[] values = prop.split(",");
			logger.info("Additional binary extensions for loading modules: " + Arrays.asList(values));
			config.setAdditionalBinaryExtensions(values);
		});

		/**
		 * By default, tokens in module files will be replaced. This property can be used to enable/disable that behavior.
		 */
		propertyConsumerMap.put("mlReplaceTokensInModules", (config, prop) -> {
			logger.info("Replace tokens in modules: " + prop);
			config.setReplaceTokensInModules(Boolean.parseBoolean(prop));
		});

		/**
		 * To mimic Roxy behavior, tokens in modules are expected to start with "@ml.". If you do not want this behavior,
		 * you can set this property to false to disable it.
		 */
		propertyConsumerMap.put("mlUseRoxyTokenPrefix", (config, prop) -> {
			logger.info("Use Roxy token prefix of '@ml.': " + prop);
			config.setUseRoxyTokenPrefix(Boolean.parseBoolean(prop));
		});

		/**
		 * Comma-separated list of paths for loading modules. Defaults to src/main/ml-modules.
		 */
		propertyConsumerMap.put("mlModulePaths", (config, prop) -> {
			logger.info("Module paths: " + prop);
			String[] paths = prop.split(",");
			// Ensure we have a modifiable list
			List<String> list = new ArrayList<>();
			for (String s : paths) {
				list.add(s);
			}
			config.setModulePaths(list);
		});

		propertyConsumerMap.put("mlModuleTimestampsPath", (config, prop) -> {
			if (prop.trim().length() == 0) {
				logger.info("Disabling use of module timestamps file");
				config.setModuleTimestampsPath(null);
			} else {
				logger.info("Module timestamps path: " + prop);
				config.setModuleTimestampsPath(prop);
			}
		});

		propertyConsumerMap.put("mlModulesRegex", (config, prop) -> {
			logger.info("Including module filenames matching regex: " + prop);
			config.setModuleFilenamesIncludePattern(Pattern.compile(prop));
		});

		/**
		 * Whether or not to load asset modules in bulk - i.e. in one transaction. Defaults to true.
		 */
		propertyConsumerMap.put("mlBulkLoadAssets", (config, prop) -> {
			logger.info("Bulk load modules: " + prop);
			config.setBulkLoadAssets(Boolean.parseBoolean(prop));
		});

		/**
		 * Whether or not to statically check asset modules after they're loaded - defaults to false.
		 */
		propertyConsumerMap.put("mlStaticCheckAssets", (config, prop) -> {
			logger.info("Statically check asset modules: " + prop);
			config.setStaticCheckAssets(Boolean.parseBoolean(prop));
		});

		/**
		 * Whether or not to attempt to statically check asset library modules after they're loaded - defaults to false.
		 * If mlStaticCheckAssets is true and this is false, and no errors will be thrown for library modules.
		 * See XccAssetLoader in ml-javaclient-util for information on how this tries to check a library module.
		 */
		propertyConsumerMap.put("mlStaticCheckLibraryAssets", (config, prop) -> {
			logger.info("Statically check asset library modules: " + prop);
			config.setStaticCheckLibraryAssets(Boolean.parseBoolean(prop));
		});

		propertyConsumerMap.put("mlDeleteTestModules", (config, prop) -> {
			logger.info("Delete test modules: " + prop);
			config.setDeleteTestModules(Boolean.parseBoolean(prop));
		});

		propertyConsumerMap.put("mlDeleteTestModulesPattern", (config, prop) -> {
			logger.info("Delete test modules pattern: " + prop);
			config.setDeleteTestModulesPattern(prop);
		});

		propertyConsumerMap.put("mlModulesLoaderThreadCount", (config, prop) -> {
			logger.info("Modules loader thread count: " + prop);
			config.setModulesLoaderThreadCount(Integer.parseInt(prop));
		});

		propertyConsumerMap.put("mlModulesLoaderBatchSize", (config, prop) -> {
			logger.info("Modules loader batch size: " + prop);
			config.setModulesLoaderBatchSize(Integer.parseInt(prop));
		});

		/**
		 * The following properties are all for generating Entity Services artifacts.
		 */
		propertyConsumerMap.put("mlModelsDatabase", (config, prop) -> {
			logger.info("Entity Services models database: " + prop);
			config.setModelsDatabase(prop);
		});

		propertyConsumerMap.put("mlModelsPath", (config, prop) -> {
			logger.info("Entity Services models path: " + prop);
			config.setModelsPath(prop);
		});

		propertyConsumerMap.put("mlInstanceConverterPath", (config, prop) -> {
			logger.info("Entity Services instance converter path: " + prop);
			config.setInstanceConverterPath(prop);
		});

		propertyConsumerMap.put("mlGenerateInstanceConverter", (config, prop) -> {
			logger.info("Entity Services generate instance converter: " + prop);
			config.setGenerateInstanceConverter(Boolean.parseBoolean(prop));
		});

		propertyConsumerMap.put("mlGenerateSchema", (config, prop) -> {
			logger.info("Entity Services generate schema: " + prop);
			config.setGenerateSchema(Boolean.parseBoolean(prop));
		});

		propertyConsumerMap.put("mlGenerateSearchOptions", (config, prop) -> {
			logger.info("Entity Services generate search options: " + prop);
			config.setGenerateSearchOptions(Boolean.parseBoolean(prop));
		});

		propertyConsumerMap.put("mlGenerateDatabaseProperties", (config, prop) -> {
			logger.info("Entity Services generate database properties: " + prop);
			config.setGenerateDatabaseProperties(Boolean.parseBoolean(prop));
		});

		propertyConsumerMap.put("mlGenerateExtractionTemplate", (config, prop) -> {
			logger.info("Entity Services generate extraction template: " + prop);
			config.setGenerateExtractionTemplate(Boolean.parseBoolean(prop));
		});

		// End Entity Services properties

		/**
		 * Sets resource filenames to ignore on ALL commands. Be careful here, in case you have files for different kinds
		 * of resources, but with the same filename (this should be very rare and easily avoided).
		 *
		 * Also that as of version 2.6.0 of ml-app-deployer, this property is processed by AbstractAppDeployer, NOT by
		 * the Command itself. So in order for this property to be applied, you must execute a Command via a subclass of
		 * AbstractAppDeployer (most commonly SimpleAppDeployer).
		 */
		propertyConsumerMap.put("mlResourceFilenamesToIgnore", (config, prop) -> {
			String[] values = prop.split(",");
			logger.info("Ignoring resource filenames: " + Arrays.asList(values));
			config.setResourceFilenamesToIgnore(values);
		});

		propertyConsumerMap.put("mlResourceFilenamesToExcludeRegex", (config, prop) -> {
			logger.info("Excluding resource filenames matching regex: " + prop);
			config.setResourceFilenamesExcludePattern(Pattern.compile(prop));
		});

		propertyConsumerMap.put("mlResourceFilenamesToIncludeRegex", (config, prop) -> {
			logger.info("Including resource filenames matching regex: " + prop);
			config.setResourceFilenamesIncludePattern(Pattern.compile(prop));
		});

		propertyConsumerMap.put("mlExcludeProperties", (config, prop) -> {
			String[] values = prop.split(",");
			logger.info("Will exclude these properties from all resource payloads: " + Arrays.asList(values));
			config.setExcludeProperties(values);
		});

		propertyConsumerMap.put("mlIncludeProperties", (config, prop) -> {
			String[] values = prop.split(",");
			logger.info("Will include only these properties in all resource payloads: " + Arrays.asList(values));
			config.setIncludeProperties(values);
		});

		propertyConsumerMap.put("mlIncremental", (config, prop) -> {
			logger.info("Supported resources will only be deployed if their resource files are now or have been modified since the last deployment: " + prop);
			config.setIncrementalDeploy(Boolean.parseBoolean(prop));
    });
    
		propertyConsumerMap.put("mlUpdateMimetypeWhenPropertiesAreEqual", (config, prop) -> {
			logger.info("Update mimetype when properties are equal (defaults to false to avoid unnecessary ML restarts): " + prop);
			config.setUpdateMimetypeWhenPropertiesAreEqual(Boolean.parseBoolean(prop));
		});
	}

	@Override
	public AppConfig newAppConfig() {
		final AppConfig appConfig = new AppConfig();
		for (String propertyName : propertyConsumerMap.keySet()) {
			String value = getProperty(propertyName);
			if (value != null) {
				propertyConsumerMap.get(propertyName).accept(appConfig, value);
			}
		}
		return appConfig;
	}

	protected Map<String, String> buildMapFromCommaDelimitedString(String str) {
		Map<String, String> map = new HashMap<>();
		String[] tokens = str.split(",");
		for (int i = 0; i < tokens.length; i += 2) {
			map.put(tokens[i], tokens[i + 1]);
		}
		return map;
	}

	protected Map<String, List<String>> buildMapOfListsFromDelimitedString(String str) {
		String[] tokens = str.split(",");
		Map<String, List<String>> map = new LinkedHashMap<>();
		for (int i = 0; i < tokens.length; i += 2) {
			String dbName = tokens[i];
			String[] hostNames = tokens[i + 1].split("\\|");
			List<String> names = new ArrayList<>();
			for (String name : hostNames) {
				names.add(name);
			}
			map.put(dbName, names);
		}
		return map;
	}

	/**
	 * This is provided so that a client can easily print out a list of all the supported properties.
	 *
	 * @return
	 */
	public Map<String, BiConsumer<AppConfig, String>> getPropertyConsumerMap() {
		return propertyConsumerMap;
	}
}
