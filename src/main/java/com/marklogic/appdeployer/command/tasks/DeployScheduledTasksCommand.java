package com.marklogic.appdeployer.command.tasks;

import java.io.File;

import com.marklogic.appdeployer.command.AbstractResourceCommand;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.SortOrderConstants;
import com.marklogic.mgmt.ResourceManager;
import com.marklogic.mgmt.tasks.TaskManager;

public class DeployScheduledTasksCommand extends AbstractResourceCommand {

    private String groupName;

    public DeployScheduledTasksCommand() {
        setExecuteSortOrder(SortOrderConstants.DEPLOY_SCHEDULED_TASKS);
        setUndoSortOrder(SortOrderConstants.DELETE_SCHEDULED_TASKS);
    }

    @Override
    protected File[] getResourceDirs(CommandContext context) {
        return new File[] { new File(context.getAppConfig().getConfigDir().getBaseDir(), "tasks") };
    }

    @Override
    protected ResourceManager getResourceManager(CommandContext context) {
        TaskManager mgr = new TaskManager(context.getManageClient());
        if (groupName != null) {
            mgr.setGroupName(groupName);
        }
        return mgr;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
