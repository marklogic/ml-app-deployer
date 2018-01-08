package com.marklogic.appdeployer.command.databases;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.appdeployer.AppConfig;
import com.marklogic.appdeployer.ConfigDir;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.SortOrderConstants;
import com.marklogic.mgmt.SaveReceipt;
import com.marklogic.mgmt.resource.databases.DatabaseManager;
import com.marklogic.rest.util.JsonNodeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * For ease of use, this command handles creating forests the the content database, either based on a file in the
 * forests directory, or based on the default payload in the DeployForestsCommand class. This allows a developer to only
 * have to define a content database file and not have to define a forest file as well. Note that if no content database
 * file exists, then this command won't do anything, and it's then expected that a content database is created via the
 * command for creating a REST API instance.
 */
public class DeployContentDatabasesCommand extends DeployDatabaseCommand {

    public DeployContentDatabasesCommand() {
        // Same default as /v1/rest-apis
        this(3);
    }

    public DeployContentDatabasesCommand(int forestsPerHost) {
        setExecuteSortOrder(SortOrderConstants.DEPLOY_CONTENT_DATABASES);
        setUndoSortOrder(SortOrderConstants.DELETE_CONTENT_DATABASES);
        setForestsPerHost(forestsPerHost);
        setForestFilename("content-forest.json");
    }

    /**
     * Calls the parent method and then adds support for a test content database.
     */
    @Override
    public void execute(CommandContext context) {
        Integer count = context.getAppConfig().getContentForestsPerHost();
        if (count != null) {
            this.setForestsPerHost(count);
        }

        super.execute(context);

        AppConfig appConfig = context.getAppConfig();
        if (appConfig.isTestPortSet()) {
            String payload = getPayload(context);
            if (payload != null) {
                DatabaseManager dbMgr = new DatabaseManager(context.getManageClient());
                String json = payloadTokenReplacer.replaceTokens(payload, appConfig, true);
                SaveReceipt receipt = dbMgr.save(json);
	            if (shouldCreateForests(context, payload)) {
		            buildDeployForestsCommand(payload, receipt, context).execute(context);
	            }
            }
        }
    }

    /**
     * Just because there's not a content database file doesn't mean that one wasn't created via the command for
     * creating a REST API server. If the REST API server command didn't delete the content database, we'd still want
     * this command to attempt to do so in the event that no content database files exist.
     */
    @Override
    public void undo(CommandContext context) {
        AppConfig appConfig = context.getAppConfig();
        JsonNode node = mergeContentDatabaseFiles(appConfig);
        if (node != null) {
            logger.info("No content database files found, so not deleting content databases");
            String payload = node.toString();
            String json = payloadTokenReplacer.replaceTokens(payload, appConfig, false);

            DatabaseManager dbMgr = newDatabaseManageForDeleting(context);

            // remove subdatabases if they exist
            removeSubDatabases(dbMgr, context, dbMgr.getResourceId(json));

            dbMgr.delete(json);
            if (appConfig.isTestPortSet()) {
                json = payloadTokenReplacer.replaceTokens(payload, appConfig, true);
                dbMgr.delete(json);
            }
        } else {
            // Try to delete the content database if it exists
            DatabaseManager dbMgr = newDatabaseManageForDeleting(context);
            dbMgr.deleteByName(appConfig.getContentDatabaseName());

            if (appConfig.isTestPortSet()) {
                dbMgr.deleteByName(appConfig.getContentDatabaseName());
            }
        }
    }

    @Override
    protected String getPayload(CommandContext context) {
        JsonNode node = mergeContentDatabaseFiles(context.getAppConfig());
        if (node == null) {
            logger.info("No content database files found, so not processing");
            return null;
        }
        return node.toString();
    }

    protected JsonNode mergeContentDatabaseFiles(AppConfig appConfig) {
        List<File> files = new ArrayList<>();
        for (ConfigDir configDir : appConfig.getConfigDirs()) {
        	List<File> list = configDir.getContentDatabaseFiles();
        	if (list != null && !list.isEmpty()) {
        		files.addAll(list);
	        }
        }
        if (logger.isInfoEnabled()) {
            logger.info("Merging JSON files at locations: " + files);
        }
        return JsonNodeUtil.mergeJsonFiles(files);
    }

}
