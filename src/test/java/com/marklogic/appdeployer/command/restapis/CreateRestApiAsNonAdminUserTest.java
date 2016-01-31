package com.marklogic.appdeployer.command.restapis;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.marklogic.junit.PermissionsFragment;
import com.marklogic.xcc.template.XccTemplate;
import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.appdeployer.ConfigDir;
import com.marklogic.appdeployer.command.modules.LoadModulesCommand;
import com.marklogic.appdeployer.command.security.DeployRolesCommand;
import com.marklogic.appdeployer.command.security.DeployUsersCommand;
import com.marklogic.mgmt.ManageClient;
import com.marklogic.mgmt.ManageConfig;

public class CreateRestApiAsNonAdminUserTest extends AbstractAppDeployerTest {

    @Autowired
    private ManageConfig manageConfig;

    private XccTemplate xccTemplate;

    @Before
    public void setup() {
        xccTemplate = newModulesXccTemplate();
        deleteModuleTimestampsFile();
    }

    @After
    public void teardown() {
        undeploySampleApp();
    }

    @Test
    public void test() {
        // Use config specific to this test
        appConfig.setConfigDir(new ConfigDir(new File("src/test/resources/non-admin-test/ml-config")));
        appConfig.getModulePaths().clear();
        appConfig.getModulePaths().add("src/test/resources/non-admin-test/ml-modules");

        // Now rebuild ManageClient using a ManageConfig that doesn't require the admin user
        ManageConfig newConfig = new ManageConfig(manageConfig.getHost(), manageConfig.getPort(),
                "sample-app-manage-admin", "sample-app-manage-admin");
        newConfig.setAdminUsername(manageConfig.getUsername());
        newConfig.setAdminPassword(manageConfig.getPassword());
        this.manageClient = new ManageClient(newConfig);

        // And ensure we use our custom user for loading modules; the custom app role has the privileges required for
        // inserting modules via XCC
        appConfig.setRestAdminUsername("sample-app-rest-admin");
        appConfig.setRestAdminPassword("sample-app-rest-admin");

        initializeAppDeployer(new DeployRestApiServersCommand(), new DeployRolesCommand(), new DeployUsersCommand(),
                new LoadModulesCommand());
        appDeployer.deploy(appConfig);

        // And now ensure that the module was loaded correctly
        PermissionsFragment perms = getDocumentPermissions("/ext/hello-lib.xqy", xccTemplate);
        perms.assertPermissionCount(3);
        perms.assertPermissionExists("rest-admin", "read");
        perms.assertPermissionExists("rest-admin", "update");
        perms.assertPermissionExists("rest-extension-user", "execute");
    }
}
