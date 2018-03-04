package com.marklogic.appdeployer.command.hosts;

import com.marklogic.appdeployer.command.AbstractUndoableCommand;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.SortOrderConstants;

public class AssignHostsToGroupsCommand extends AbstractUndoableCommand {

    public AssignHostsToGroupsCommand() {
        setExecuteSortOrder(SortOrderConstants.ASSIGN_HOSTS_TO_GROUPS);
        setUndoSortOrder(SortOrderConstants.UNASSIGN_HOSTS_FROM_GROUPS);
    }

	@Override
	public void execute(CommandContext context) {
		System.out.println("AAAAA");
	}

	@Override
	public void undo(CommandContext context) {
		// TODO Auto-generated method stub
		
	}
}
