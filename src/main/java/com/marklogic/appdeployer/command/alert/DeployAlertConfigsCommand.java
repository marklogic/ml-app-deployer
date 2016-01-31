package com.marklogic.appdeployer.command.alert;

import java.io.File;

import com.marklogic.appdeployer.command.AbstractResourceCommand;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.SortOrderConstants;
import com.marklogic.mgmt.ResourceManager;
import com.marklogic.mgmt.alert.AlertConfigManager;

/**
 * Defaults to the content database name in the AppConfig instance. Can be overridden via the databaseNameOrId property.
 */
public class DeployAlertConfigsCommand extends AbstractResourceCommand {

    private String databaseIdOrName;

    /**
     * TODO Can support "undo" in the future, which would just involve deleting the alert config from the content
     * database.
     */
    public DeployAlertConfigsCommand() {
        setExecuteSortOrder(SortOrderConstants.DEPLOY_ALERT_CONFIGS);
        setDeleteResourcesOnUndo(false);
    }

    /**
     * Config documents will be stored in "alert/configs", and then actions can be stored in a subfolder named
     * "(uri)-actions".
     */
    @Override
    protected File[] getResourceDirs(CommandContext context) {
        return new File[] { new File(context.getAppConfig().getConfigDir().getAlertDir(), "configs") };
    }

    @Override
    protected ResourceManager getResourceManager(CommandContext context) {
        String db = databaseIdOrName != null ? databaseIdOrName : context.getAppConfig().getContentDatabaseName();
        return new AlertConfigManager(context.getManageClient(), db);
    }

    public void setDatabaseIdOrName(String databaseIdOrName) {
        this.databaseIdOrName = databaseIdOrName;
    }
}
