package com.marklogic.appdeployer.command.groups;

import java.io.File;

import com.marklogic.appdeployer.command.AbstractResourceCommand;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.SortOrderConstants;
import com.marklogic.mgmt.SaveReceipt;
import com.marklogic.mgmt.api.server.Server;
import com.marklogic.mgmt.resource.ResourceManager;
import com.marklogic.mgmt.resource.appservers.ServerManager;
import com.marklogic.mgmt.resource.groups.GroupManager;
import com.marklogic.rest.util.Fragment;

public class DeployGroupsCommand extends AbstractResourceCommand {

	private Server adminServerTemplate;
	private static final String ADMIN_SERVER_NAME = "Admin";
	private static final String DEFAULT_ADMIN_SERVER_URL_REWRITER = "rewriter.xqy";

    public DeployGroupsCommand() {
        setExecuteSortOrder(SortOrderConstants.DEPLOY_GROUPS);
        setUndoSortOrder(SortOrderConstants.DELETE_GROUPS);
        adminServerTemplate = new Server(null, ADMIN_SERVER_NAME);
    	adminServerTemplate.setUrlRewriter(DEFAULT_ADMIN_SERVER_URL_REWRITER);
    }
    
	public void setAdminServerTemplate(Server adminServerTemplate) {
		this.adminServerTemplate = adminServerTemplate;
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
        	Fragment xml = new Fragment(payload);
        	String groupName = xml.getElementValue("/m:group-properties/m:group-name");
			ServerManager serverMgr = new ServerManager(context.getManageClient(), groupName);
			serverMgr.save(adminServerTemplate.getJson());
        }
    }
}
