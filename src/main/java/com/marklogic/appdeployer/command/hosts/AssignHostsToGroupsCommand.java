package com.marklogic.appdeployer.command.hosts;

import java.util.Map;

import com.marklogic.appdeployer.command.AbstractUndoableCommand;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.SortOrderConstants;
import com.marklogic.mgmt.admin.AdminManager;
import com.marklogic.mgmt.resource.appservers.ServerManager;
import com.marklogic.mgmt.resource.hosts.HostManager;

public class AssignHostsToGroupsCommand extends AbstractUndoableCommand {

	private static final String MANAGE_SERVER_NAME = "Manage";
	private static final String MANAGE_SERVER_JSON_TEMPLATE = "{\"server-name\":\"%s\", \"group-name\":\"%s\", \"server-type\":\"http\", \"enabled\":true, \"root\":\"Apps/\", \"authentication\":\"digest\", \"port\":8002, \"webDAV\":false, \"execute\":true, \"display-last-login\":false, \"address\":\"0.0.0.0\", \"backlog\":512, \"threads\":32, \"request-timeout\":30, \"keep-alive-timeout\":5, \"session-timeout\":3600, \"max-time-limit\":3600, \"default-time-limit\":600, \"max-inference-size\":500, \"default-inference-size\":100, \"static-expires\":3600, \"pre-commit-trigger-depth\":1000, \"pre-commit-trigger-limit\":10000, \"collation\":\"http://marklogic.com/collation/\", \"internal-security\":true, \"concurrent-request-limit\":0, \"compute-content-length\":true, \"log-errors\":false, \"debug-allow\":true, \"profile-allow\":true, \"default-xquery-version\":\"1.0-ml\", \"multi-version-concurrency-control\":\"contemporaneous\", \"distribute-timestamps\":\"fast\", \"output-sgml-character-entities\":\"none\", \"output-encoding\":\"UTF-8\", \"output-method\":\"default\", \"output-byte-order-mark\":\"default\", \"output-cdata-section-namespace-uri\":\"\", \"output-cdata-section-localname\":null, \"output-doctype-public\":\"\", \"output-doctype-system\":\"\", \"output-escape-uri-attributes\":\"default\", \"output-include-content-type\":\"default\", \"output-indent\":\"default\", \"output-indent-untyped\":\"default\", \"output-indent-tabs\":\"default\", \"output-media-type\":\"\", \"output-normalization-form\":\"none\", \"output-omit-xml-declaration\":\"default\", \"output-standalone\":\"omit\", \"output-undeclare-prefixes\":\"default\", \"output-version\":\"\", \"output-include-default-attributes\":\"default\", \"default-error-format\":\"compatible\", \"error-handler\":\"manage/error-handler.xqy\", \"url-rewriter\":\"manage/rewriter.xqy\", \"rewrite-resolves-globally\":false, \"ssl-allow-sslv3\":true, \"ssl-allow-tls\":true, \"ssl-disable-sslv3\":false, \"ssl-disable-tlsv1\":false, \"ssl-disable-tlsv1-1\":false, \"ssl-disable-tlsv1-2\":false, \"ssl-hostname\":\"\", \"ssl-ciphers\":\"ALL:!LOW:@STRENGTH\", \"ssl-require-client-certificate\":true, \"content-database\":\"App-Services\", \"default-user\":\"nobody\", \"privilege\":\"http://marklogic.com/xdmp/privileges/manage\"}";
	
    public AssignHostsToGroupsCommand() {
        setExecuteSortOrder(SortOrderConstants.ASSIGN_HOSTS_TO_GROUPS);
        setUndoSortOrder(SortOrderConstants.UNASSIGN_HOSTS_FROM_GROUPS);
    }

	@Override
	public void execute(CommandContext context) {
		Map<String, String> hostGroups = context.getAppConfig().getHostGroups();
	    AdminManager adminManager = context.getAdminManager();
		HostManager mgr = new HostManager(context.getManageClient());
		for (Map.Entry<String, String> entry : hostGroups.entrySet()) {
			String hostName = entry.getKey();
			String groupName = entry.getValue();
			// First, ensure a Manage server exists for the group
			ServerManager serverMgr = new ServerManager(context.getManageClient(), groupName);
			try {
				String serverResponse = serverMgr.getPropertiesAsJson(MANAGE_SERVER_NAME, "group-id="+groupName);
				System.out.println("serverResponse: " + serverResponse);
	        } catch (Exception e) {
	        	if (e.getMessage().contains("404 Not Found")) {
	                String manageServerPayload = format(MANAGE_SERVER_JSON_TEMPLATE, MANAGE_SERVER_NAME, groupName);
	        		serverMgr.save(manageServerPayload);
	        	} else {
	        		logger.error("Error checking for Manage server in target group: " + e.getMessage());
	        		throw e;
	        	}
	        }
			mgr.setHostToGroup(hostName, groupName);
			adminManager.waitForRestart();
		}
	}


	@Override
	public void undo(CommandContext context) {
		Map<String, String> hostGroups = context.getAppConfig().getHostGroups();
	    AdminManager adminManager = context.getAdminManager();
		HostManager mgr = new HostManager(context.getManageClient());
		for (Map.Entry<String, String> entry : hostGroups.entrySet()) {
			mgr.setHostToGroup(entry.getKey(), "Default");
			adminManager.waitForRestart();
		}
	}
}
