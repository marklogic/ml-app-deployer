package com.marklogic.appdeployer.command.hosts;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.marklogic.appdeployer.command.AbstractUndoableCommand;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.SortOrderConstants;
import com.marklogic.mgmt.resource.hosts.HostManager;

public class AssignHostsToGroupsCommand extends AbstractUndoableCommand {

    public AssignHostsToGroupsCommand() {
        setExecuteSortOrder(SortOrderConstants.ASSIGN_HOSTS_TO_GROUPS);
        setUndoSortOrder(SortOrderConstants.UNASSIGN_HOSTS_FROM_GROUPS);
    }

	@Override
	public void execute(CommandContext context) {
		System.out.println("Executing AssignHostsToGroupsCommand");
		Map<String, String> hostGroups = context.getAppConfig().getHostGroups();
		HostManager mgr = new HostManager(context.getManageClient());
		for (Map.Entry<String, String> entry : hostGroups.entrySet()) {
			System.out.println(format("%s -> %s", entry.getKey(), entry.getValue()));
			ResponseEntity<String> response = mgr.setHostToGroup(entry.getKey(), entry.getValue());
			System.out.println("Status Code: " + response.getStatusCode());
		}
	}


	@Override
	public void undo(CommandContext context) {
		// TODO Auto-generated method stub
		
	}
}
