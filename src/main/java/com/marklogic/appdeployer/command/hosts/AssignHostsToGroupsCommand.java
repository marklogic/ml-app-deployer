package com.marklogic.appdeployer.command.hosts;

import java.util.Map;

import com.marklogic.appdeployer.command.AbstractUndoableCommand;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.SortOrderConstants;
import com.marklogic.mgmt.api.server.AppServicesServer;
import com.marklogic.mgmt.api.server.ManageServer;
import com.marklogic.mgmt.api.server.Server;
import com.marklogic.mgmt.resource.appservers.ServerManager;
import com.marklogic.mgmt.resource.hosts.HostManager;

public class AssignHostsToGroupsCommand extends AbstractUndoableCommand {

	private static final String DEFAULT_GROUP_NAME = "Default";
	private Server manageServerTemplate;
	private Server appServicesServerTemplate;

	public AssignHostsToGroupsCommand() {
        setExecuteSortOrder(SortOrderConstants.ASSIGN_HOSTS_TO_GROUPS);
        setUndoSortOrder(SortOrderConstants.UNASSIGN_HOSTS_FROM_GROUPS);
        manageServerTemplate = new ManageServer();
        appServicesServerTemplate = new AppServicesServer();
    }

	public void setManageServerTemplate(Server manageServerTemplate) {
		this.manageServerTemplate = manageServerTemplate;
	}
    
	public void setAppServicesServerTemplate(Server appServicesServerTemplate) {
		this.appServicesServerTemplate = appServicesServerTemplate;
	}

	@Override
	public void execute(CommandContext context) {
		context.getAdminManager().invokeActionRequiringRestart(() -> assignHostsToGroups(context)); 
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
			if (serverMgr.exists(ManageServer.MANAGE_SERVER_NAME)) {
				logger.info(format("%s appserver already exists in target group %s", ManageServer.MANAGE_SERVER_NAME, groupName));
			} else {
				manageServerTemplate.setGroupName(groupName);
	        	serverMgr.save(manageServerTemplate.getJson());
				logger.info(format("Created the %s appserver in target group %s", ManageServer.MANAGE_SERVER_NAME, groupName));
	        }
			if (serverMgr.exists(AppServicesServer.APP_SERVICES_SERVER_NAME)) {
				logger.info(format("%s appserver already exists in target group %s", AppServicesServer.APP_SERVICES_SERVER_NAME, groupName));
			} else {
				appServicesServerTemplate.setGroupName(groupName);
	        	serverMgr.save(appServicesServerTemplate.getJson());
				logger.info(format("Created the %s appserver in target group %s", AppServicesServer.APP_SERVICES_SERVER_NAME, groupName));
	        }
			
			

			if (!groupName.equals(hostMgr.getAssignedGroupName(hostName))) {
				hostMgr.setHostToGroup(hostName, groupName);
				requiresRestart = true;
			}
		}
		return requiresRestart;
	}


	@Override
	public void undo(CommandContext context) {
		context.getAdminManager().invokeActionRequiringRestart(() -> assignHostsToDefault(context)); 
	}
	
	protected boolean assignHostsToDefault(CommandContext context) {
		boolean requiresRestart = false;
		Map<String, String> hostGroups = context.getAppConfig().getHostGroups();
		HostManager hostMgr = new HostManager(context.getManageClient());
		for (Map.Entry<String, String> entry : hostGroups.entrySet()) {
			String hostName = entry.getKey();
			if (hostMgr.getAssignedGroupName(hostName) != DEFAULT_GROUP_NAME) {
				hostMgr.setHostToGroup(hostName, DEFAULT_GROUP_NAME);
				requiresRestart = true;
			}
		}
		return requiresRestart;
	}
}
