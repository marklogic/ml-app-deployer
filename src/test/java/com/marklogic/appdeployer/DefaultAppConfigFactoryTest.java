package com.marklogic.appdeployer;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.ext.SecurityContextType;
import org.junit.Assert;
import org.junit.Test;

import com.marklogic.mgmt.util.SimplePropertySource;

public class DefaultAppConfigFactoryTest extends Assert {

    private DefaultAppConfigFactory sut;

    @Test
    public void gradleStyleProperties() {
        sut = new DefaultAppConfigFactory(new SimplePropertySource("mlHost", "somehost", "mlUsername", "someuser"));
        AppConfig config = sut.newAppConfig();
        assertEquals("somehost", config.getHost());
        assertEquals("someuser", config.getRestAdminUsername());
    }

    @Test
    public void springStyleProperties() {
        sut = new DefaultAppConfigFactory(
                new SimplePropertySource("marklogic.mlHost", "springhost", "marklogic.mlUsername", "springuser"));
        AppConfig config = sut.newAppConfig();
        assertEquals("springhost", config.getHost());
        assertEquals("springuser", config.getRestAdminUsername());
    }

    @Test
    public void unrecognizedProperties() {
        sut = new DefaultAppConfigFactory(new SimplePropertySource("foo.mlHost", "host", "foo.mlUsername", "user"));
        AppConfig config = sut.newAppConfig();
        assertEquals("Should use default", "localhost", config.getHost());
        assertEquals("Should use default", "admin", config.getRestAdminUsername());
    }

    @Test
    public void appServicesDefaultsToDefaultUsernamePassword() {
	    sut = new DefaultAppConfigFactory(new SimplePropertySource("mlUsername", "someuser", "mlPassword", "somepassword"));
	    AppConfig config = sut.newAppConfig();
	    assertEquals("someuser", config.getRestAdminUsername());
	    assertEquals("somepassword", config.getRestAdminPassword());
	    assertEquals("someuser", config.getAppServicesUsername());
	    assertEquals("somepassword", config.getAppServicesPassword());
    }

	@Test
	public void appServicesDefaultsToRestAdminUsernamePassword() {
		sut = new DefaultAppConfigFactory(new SimplePropertySource("mlRestAdminUsername", "someuser", "mlRestAdminPassword", "somepassword"));
		AppConfig config = sut.newAppConfig();
		assertEquals("someuser", config.getRestAdminUsername());
		assertEquals("somepassword", config.getRestAdminPassword());
		assertEquals("someuser", config.getAppServicesUsername());
		assertEquals("somepassword", config.getAppServicesPassword());
	}

	@Test
	public void appServicesDiffersFromRestAdmin() {
		sut = new DefaultAppConfigFactory(new SimplePropertySource(
			"mlRestAdminUsername", "someuser", "mlRestAdminPassword", "somepassword",
			"mlAppServicesUsername", "appuser", "mlAppServicesPassword", "appword"));
		AppConfig config = sut.newAppConfig();
		assertEquals("someuser", config.getRestAdminUsername());
		assertEquals("somepassword", config.getRestAdminPassword());
		assertEquals("appuser", config.getAppServicesUsername());
		assertEquals("appword", config.getAppServicesPassword());
	}

	/**
     * As of 2.2.0.
     */
    @Test
    public void allProperties() {
        Properties p = new Properties();
        p.setProperty("mlHost", "prophost");
        p.setProperty("mlAppName", "propname");
        p.setProperty("mlNoRestServer", "true");
        p.setProperty("mlUsername", "propuser1");
        p.setProperty("mlPassword", "proppassword");

	    p.setProperty("mlRestPort", "4321");
	    p.setProperty("mlTestRestPort", "8765");
        p.setProperty("mlRestAdminUsername", "propuser2");
        p.setProperty("mlRestAdminPassword", "proppassword2");
        p.setProperty("mlRestAuthentication", "certiFicate");
        p.setProperty("mlRestCertFile", "restCertFile");
        p.setProperty("mlRestCertPassword", "restCertPassword");
        p.setProperty("mlRestExternalName", "restExternalName");

	    p.setProperty("mlAppServicesUsername", "appServicesUsername");
	    p.setProperty("mlAppServicesPassword", "appServicesPassword");
	    p.setProperty("mlAppServicesPort", "8123");
	    p.setProperty("mlAppServicesAuthentication", "kerBEROS");
	    p.setProperty("mlAppServicesCertFile", "appServicesCertFile");
	    p.setProperty("mlAppServicesCertPassword", "appServicesCertPassword");
	    p.setProperty("mlAppServicesExternalName", "appServicesExternalName");
	    p.setProperty("mlAppServicesSimpleSsl", "true");

	    p.setProperty("mlContentForestsPerHost", "17");
	    p.setProperty("mlCreateForests", "false");
	    p.setProperty("mlForestsPerHost", "some-db,2,other-db,3");
        p.setProperty("mlModulePermissions", "some-perm,read,some-perm,update");
        p.setProperty("mlAdditionalBinaryExtensions", ".gradle,.properties");
        p.setProperty("mlConfigPath", "src/test/resources/sample-app/empty-ml-config");
        p.setProperty("mlSimpleSsl", "true");
        p.setProperty("mlContentDatabaseName", "my-content-db");
        p.setProperty("mlModulesDatabaseName", "my-modules");
        p.setProperty("mlSchemasDatabaseName", "my-schemas-db");
        p.setProperty("mlTriggersDatabaseName", "my-triggers-db");
        p.setProperty("mlSchemasPath", "/my/schemas");
	    p.setProperty("mlDeleteForests", "false");
        p.setProperty("mlDeleteReplicas", "false");
        p.setProperty("mlGroupName", "other-group");
        p.setProperty("mlReplaceTokensInModules", "false");
        p.setProperty("mlUseRoxyTokenPrefix", "false");
        p.setProperty("mlModulePaths", "path1,path2,path3");
        p.setProperty("mlModuleTimestampsPath", "custom/timestamps/path.properties");
        p.setProperty("mlDeleteTestModules", "true");
        p.setProperty("mlDeleteTestModulesPattern", "/some/pattern");
        p.setProperty("mlModulesLoaderThreadCount", "3");

        p.setProperty("mlModelsPath", "ml/models");
        p.setProperty("mlInstanceConverterPath", "ext/my/path");
        p.setProperty("mlGenerateInstanceConverter", "false");
	    p.setProperty("mlGenerateDatabaseProperties", "false");
	    p.setProperty("mlGenerateSchema", "false");
	    p.setProperty("mlGenerateSearchOptions", "false");
	    p.setProperty("mlGenerateExtractionTemplate", "false");

	    p.setProperty("mlResourceFilenamesToIgnore", "role1.json,role2.xml");
	    p.setProperty("mlResourceFilenamesToExcludeRegex", "dev-.*");
	    p.setProperty("mlResourceFilenamesToIncludeRegex", "qa-.*");

	    p.setProperty("mlDatabaseNamesAndReplicaCounts", "Documents,1,Security,2");

	    p.setProperty("mlForestDataDirectory", "/data/path");
	    p.setProperty("mlForestFastDataDirectory", "/fast/path");
	    p.setProperty("mlForestLargeDataDirectory", "/large/path");

	    p.setProperty("mlReplicaForestDataDirectory", "/var/data");
	    p.setProperty("mlReplicaForestFastDataDirectory", "/var/fast");
	    p.setProperty("mlReplicaForestLargeDataDirectory", "/var/large");

	    p.setProperty("mlDatabaseDataDirectories", "Documents,/data/documents,Security,/data/security");
	    p.setProperty("mlDatabaseFastDataDirectories", "Documents,/fast/documents,Security,/fast/security");
	    p.setProperty("mlDatabaseLargeDataDirectories", "Documents,/large/documents,Security,/large/security");

	    p.setProperty("mlDatabaseReplicaDataDirectories", "Documents,/data/replicas,Security,/data/security/replicas");
	    p.setProperty("mlDatabaseReplicaFastDataDirectories", "Documents,/fast/replicas,Security,/fast/security/replicas");
	    p.setProperty("mlDatabaseReplicaLargeDataDirectories", "Documents,/large/replicas,Security,/large/security/replicas");

	    p.setProperty("mlSortRolesByDependencies", "false");

	    sut = new DefaultAppConfigFactory(new SimplePropertySource(p));
        AppConfig config = sut.newAppConfig();

        assertEquals("prophost", config.getHost());
        assertEquals("propname", config.getName());
        assertTrue(config.isNoRestServer());

        // REST server connection properties
	    assertEquals((Integer) 4321, config.getRestPort());
	    assertEquals((Integer) 8765, config.getTestRestPort());
        assertEquals("propuser2", config.getRestAdminUsername());
        assertEquals("proppassword2", config.getRestAdminPassword());
	    assertEquals(DatabaseClientFactory.Authentication.CERTIFICATE, config.getRestAuthentication());
        assertEquals(SecurityContextType.CERTIFICATE, config.getRestSecurityContextType());
        assertEquals("restCertFile", config.getRestCertFile());
        assertEquals("restCertPassword", config.getRestCertPassword());
        assertEquals("restExternalName", config.getRestExternalName());

        // App-Services server connection properties
	    assertEquals("appServicesUsername", config.getAppServicesUsername());
	    assertEquals("appServicesPassword", config.getAppServicesPassword());
	    assertEquals((Integer) 8123, config.getAppServicesPort());
	    assertEquals(DatabaseClientFactory.Authentication.KERBEROS, config.getAppServicesAuthentication());
	    assertEquals(SecurityContextType.KERBEROS, config.getAppServicesSecurityContextType());
	    assertEquals("appServicesCertFile", config.getAppServicesCertFile());
	    assertEquals("appServicesCertPassword", config.getAppServicesCertPassword());
	    assertEquals("appServicesExternalName", config.getAppServicesExternalName());
	    assertNotNull(config.getAppServicesSslContext());
	    assertEquals(DatabaseClientFactory.SSLHostnameVerifier.ANY, config.getAppServicesSslHostnameVerifier());

	    assertEquals((Integer) 17, config.getContentForestsPerHost());
	    assertFalse(config.isCreateForests());
	    Map<String, Integer> forestCounts = config.getForestCounts();
	    assertEquals(2, (int)forestCounts.get("some-db"));
	    assertEquals(3, (int)forestCounts.get("other-db"));
        assertEquals("some-perm,read,some-perm,update", config.getModulePermissions());
        String[] extensions = config.getAdditionalBinaryExtensions();
        assertEquals(".gradle", extensions[0]);
        assertEquals(".properties", extensions[1]);
        assertTrue(config.getConfigDir().getBaseDir().getAbsolutePath().contains("empty-ml-config"));
        assertNotNull(config.getRestSslContext());
        assertNotNull(config.getRestSslHostnameVerifier());
        assertEquals("my-content-db", config.getContentDatabaseName());
        assertEquals("my-modules", config.getModulesDatabaseName());
        assertEquals("my-schemas-db", config.getSchemasDatabaseName());
        assertEquals("my-triggers-db", config.getTriggersDatabaseName());
        assertEquals("/my/schemas", config.getSchemasPath());
	    assertFalse(config.isDeleteForests());
        assertFalse(config.isDeleteReplicas());
        assertEquals("other-group", config.getGroupName());
        assertFalse(config.isReplaceTokensInModules());
        assertFalse(config.isUseRoxyTokenPrefix());
        assertTrue(config.isDeleteTestModules());
        assertEquals("/some/pattern", config.getDeleteTestModulesPattern());
        assertEquals(3, config.getModulesLoaderThreadCount());

        assertEquals("ml/models", config.getModelsPath());
        assertEquals("ext/my/path", config.getInstanceConverterPath());
        assertFalse(config.isGenerateDatabaseProperties());
	    assertFalse(config.isGenerateExtractionTemplate());
	    assertFalse(config.isGenerateInstanceConverter());
	    assertFalse(config.isGenerateSchema());
	    assertFalse(config.isGenerateSearchOptions());

	    List<String> paths = config.getModulePaths();
        assertEquals("path1", paths.get(0));
        assertEquals("path2", paths.get(1));
        assertEquals("path3", paths.get(2));

        assertEquals("custom/timestamps/path.properties", config.getModuleTimestampsPath());

        assertEquals("role1.json", config.getResourceFilenamesToIgnore()[0]);
	    assertEquals("role2.xml", config.getResourceFilenamesToIgnore()[1]);
	    assertEquals("dev-.*", config.getResourceFilenamesExcludePattern().pattern());
	    assertEquals("qa-.*", config.getResourceFilenamesIncludePattern().pattern());

	    assertEquals("Documents,1,Security,2", config.getDatabaseNamesAndReplicaCounts());

	    assertEquals("/data/path", config.getForestDataDirectory());
	    assertEquals("/fast/path", config.getForestFastDataDirectory());
	    assertEquals("/large/path", config.getForestLargeDataDirectory());

	    assertEquals("/var/data", config.getReplicaForestDataDirectory());
	    assertEquals("/var/fast", config.getReplicaForestFastDataDirectory());
	    assertEquals("/var/large", config.getReplicaForestLargeDataDirectory());

	    Map<String, String> map = config.getDatabaseDataDirectories();
	    assertEquals("/data/documents", map.get("Documents"));
	    assertEquals("/data/security", map.get("Security"));
	    map = config.getDatabaseFastDataDirectories();
	    assertEquals("/fast/documents", map.get("Documents"));
	    assertEquals("/fast/security", map.get("Security"));
	    map = config.getDatabaseLargeDataDirectories();
	    assertEquals("/large/documents", map.get("Documents"));
	    assertEquals("/large/security", map.get("Security"));

	    map = config.getDatabaseReplicaDataDirectories();
	    assertEquals("/data/replicas", map.get("Documents"));
	    assertEquals("/data/security/replicas", map.get("Security"));
	    map = config.getDatabaseReplicaFastDataDirectories();
	    assertEquals("/fast/replicas", map.get("Documents"));
	    assertEquals("/fast/security/replicas", map.get("Security"));
	    map = config.getDatabaseReplicaLargeDataDirectories();
	    assertEquals("/large/replicas", map.get("Documents"));
	    assertEquals("/large/security/replicas", map.get("Security"));

	    assertFalse(config.isSortRolesByDependencies());
    }

	/**
	 * Verifies that mlConfigDir is still supported, though mlConfigPath is preferred.
	 */
	@Test
	public void mlConfigDir() {
		Properties p = new Properties();
		p.setProperty("mlConfigDir", "src/test/resources/sample-app/empty-ml-config");

		sut = new DefaultAppConfigFactory(new SimplePropertySource(p));
		AppConfig config = sut.newAppConfig();
		assertTrue(config.getConfigDir().getBaseDir().getAbsolutePath().contains("empty-ml-config"));
	}

    @Test
    public void mlUsernameAndPassword() {
        sut = new DefaultAppConfigFactory(
                new SimplePropertySource("mlUsername", "customuser", "mlPassword", "custompassword"));
        AppConfig config = sut.newAppConfig();

        assertEquals("When mlRestAdminUsername is not set, mlUsername should be used", "customuser",
                config.getRestAdminUsername());
        assertEquals("When mlRestAdminPassword is not set, mlPassword should be used", "custompassword",
                config.getRestAdminPassword());

        assertNull("SSL context should be null by default", config.getRestSslContext());
        assertNull("SSL hostname verifier should be null by default", config.getRestSslHostnameVerifier());
    }
}
