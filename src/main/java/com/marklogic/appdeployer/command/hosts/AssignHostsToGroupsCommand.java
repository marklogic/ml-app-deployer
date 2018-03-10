package com.marklogic.appdeployer.command.hosts;

import java.util.Map;

import com.marklogic.appdeployer.command.AbstractUndoableCommand;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.SortOrderConstants;
import com.marklogic.mgmt.admin.ActionRequiringRestart;
import com.marklogic.mgmt.resource.appservers.ServerManager;
import com.marklogic.mgmt.resource.hosts.HostManager;

public class AssignHostsToGroupsCommand extends AbstractUndoableCommand {

	private static final String DEFAULT_GROUP_NAME = "Default";
	private static final String MANAGE_SERVER_NAME = "Manage";
	private static final String MANAGE_SERVER_JSON_TEMPLATE = "{\"server-name\":\"%s\", \"group-name\":\"%s\", \"server-type\":\"http\", \"root\":\"Apps/\", \"port\":8002, \"content-database\":\"App-Services\", \"error-handler\":\"manage/error-handler.xqy\", \"url-rewriter\":\"manage/rewriter.xqy\", \"privilege\":\"http://marklogic.com/xdmp/privileges/manage\"}";
	private static final String APP_SERVER_NAME = "App-Services";
	private static final String APP_SERVER_JSON_TEMPLATE = "{\"server-name\":\"%s\", \"group-name\":\"%s\", \"server-type\":\"http\", \"root\":\"/\", \"port\":8000, \"modules-database\":\"Modules\", \"content-database\":\"Documents\", \"error-handler\":\"/MarkLogic/rest-api/8000-error-handler.xqy\", \"url-rewriter\":\"/MarkLogic/rest-api/8000-rewriter.xml\", \"rewrite-resolves-globally\":true}";
	private static final String ADMIN_SERVER_NAME = "Admin";
	private static final String ADMIN_SERVER_UPDATE_JSON_TEMPLATE = "{\"server-name\":\"%s\", \"url-rewriter\":\"rewriter.xqy\"}";

	public AssignHostsToGroupsCommand() {
        setExecuteSortOrder(SortOrderConstants.ASSIGN_HOSTS_TO_GROUPS);
        setUndoSortOrder(SortOrderConstants.UNASSIGN_HOSTS_FROM_GROUPS);
    }

	@Override
	public void execute(CommandContext context) {
		context.getAdminManager().invokeActionRequiringRestart(new ActionRequiringRestart() {
			@Override
			public boolean execute() {
				return assignHostsToGroups(context);
			}
		});
	}
	
	protected boolean assignHostsToGroups(CommandContext context) {
		boolean requiresRestart = false;
		
		Map<String, String> hostGroups = context.getAppConfig().getHostGroups();
		HostManager hostMgr = new HostManager(context.getManageClient());
		for (Map.Entry<String, String> entry : hostGroups.entrySet()) {
			String hostName = entry.getKey();
			String groupName = entry.getValue();
			// First, ensure a Manage and App-Services servers exists for the group
			// This issue is fixed in ML 9.0-3 (https://bugtrack.marklogic.com/46909)
			ServerManager serverMgr = new ServerManager(context.getManageClient(), groupName);
			if (serverMgr.exists(MANAGE_SERVER_NAME, "group-id="+groupName)) {
				logger.info(format("%s appserver already exists in target group %s", MANAGE_SERVER_NAME, groupName));
			} else {
	        	String manageServerPayload = format(MANAGE_SERVER_JSON_TEMPLATE, MANAGE_SERVER_NAME, groupName);
	        	serverMgr.save(manageServerPayload);
				logger.info(format("Created the %s appserver in target group %s", MANAGE_SERVER_NAME, groupName));
	        }
			if (serverMgr.exists(APP_SERVER_NAME, "group-id="+groupName)) {
				logger.info(format("%s appserver already exists in target group %s", APP_SERVER_NAME, groupName));
			} else {
	        	String manageServerPayload = format(APP_SERVER_JSON_TEMPLATE, APP_SERVER_NAME, groupName);
	        	serverMgr.save(manageServerPayload);
				logger.info(format("Created the %s appserver in target group %s", APP_SERVER_NAME, groupName));
	        }

			// When new groups are created, an Admin server is automatically created in that group.
			// However, the Admin server's rewrite property is empty - causing problems with reading the timestamp
			// This fix should probably happen when the group is created.
            String adminServerPayload = format(ADMIN_SERVER_UPDATE_JSON_TEMPLATE, ADMIN_SERVER_NAME, groupName);
			serverMgr.save(adminServerPayload);
			
			hostMgr.setHostToGroup(hostName, groupName);
			requiresRestart = true;
		}
		return requiresRestart;
	}


	@Override
	public void undo(CommandContext context) {
		context.getAdminManager().invokeActionRequiringRestart(new ActionRequiringRestart() {
			@Override
			public boolean execute() {
				return assignHostsToDefault(context);
			}
		});
	}
	
	protected boolean assignHostsToDefault(CommandContext context) {
		boolean requiresRestart = false;
		Map<String, String> hostGroups = context.getAppConfig().getHostGroups();
		HostManager hostMgr = new HostManager(context.getManageClient());
		for (Map.Entry<String, String> entry : hostGroups.entrySet()) {
			hostMgr.setHostToGroup(entry.getKey(), DEFAULT_GROUP_NAME);
			requiresRestart = true;
		}
		return requiresRestart;
	}
}
