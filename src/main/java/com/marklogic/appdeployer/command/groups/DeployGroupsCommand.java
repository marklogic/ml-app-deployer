package com.marklogic.appdeployer.command.groups;

import java.io.File;

import com.marklogic.appdeployer.command.AbstractResourceCommand;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.SortOrderConstants;
import com.marklogic.mgmt.PayloadParser;
import com.marklogic.mgmt.SaveReceipt;
import com.marklogic.mgmt.api.server.Server;
import com.marklogic.mgmt.resource.ResourceManager;
import com.marklogic.mgmt.resource.appservers.ServerManager;
import com.marklogic.mgmt.resource.groups.GroupManager;

public class DeployGroupsCommand extends AbstractResourceCommand {

	private Server adminServerTemplate;

	public DeployGroupsCommand() {
        setExecuteSortOrder(SortOrderConstants.DEPLOY_GROUPS);
        setUndoSortOrder(SortOrderConstants.DELETE_GROUPS);
        adminServerTemplate = new Server(null, "Admin");
    	adminServerTemplate.setUrlRewriter("rewriter.xqy");
    }

    @Override
    protected File[] getResourceDirs(CommandContext context) {
    	return findResourceDirs(context, configDir -> configDir.getGroupsDir());
    }

    @Override
    protected ResourceManager getResourceManager(CommandContext context) {
        return new GroupManager(context.getManageClient());
    }

    /**
     * Does a poor man's job of checking for a restart by checking for "cache-size" in the payload. This doesn't mean a
     * restart has occurred - the cache size may not changed - but that's fine, as the waitForRestart method on
     * AdminManager will quickly exit.
     */
    @Override
    protected void afterResourceSaved(ResourceManager mgr, CommandContext context, File resourceFile,
            SaveReceipt receipt) {
        String payload = receipt.getPayload();
        if (payload != null) {
        	if (payload.contains("cache-size") && context.getAdminManager() != null) {
                if (logger.isDebugEnabled()) {
                    logger.info("Group payload contains cache-size parameter, so waiting for ML to restart");
                }
                context.getAdminManager().waitForRestart();
        	}
			// When new groups are created, an Admin server is automatically created in that group.
			// However, the Admin server's rewrite property is empty - causing problems with reading the timestamp
    		String groupName = new PayloadParser().getPayloadFieldValue(payload, "group-name", true);
			ServerManager serverMgr = new ServerManager(context.getManageClient(), groupName);
			serverMgr.save(adminServerTemplate.getJson());
        }
    }

    public Server getAdminServerTemplate() {
		return adminServerTemplate;
	}
    
	public void setAdminServerTemplate(Server adminServerTemplate) {
		this.adminServerTemplate = adminServerTemplate;
	}
}
