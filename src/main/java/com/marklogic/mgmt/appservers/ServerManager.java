package com.marklogic.mgmt.appservers;

import com.marklogic.mgmt.AbstractResourceManager;
import com.marklogic.mgmt.ManageClient;

public class ServerManager extends AbstractResourceManager {

    public final static String DEFAULT_GROUP = "Default";

    private String groupName;

    public ServerManager(ManageClient manageClient) {
        this(manageClient, DEFAULT_GROUP);
    }

    public ServerManager(ManageClient manageClient, String groupName) {
        super(manageClient);
        this.groupName = groupName != null ? groupName : DEFAULT_GROUP;
    }

    @Override
    public String getResourcePath(String resourceNameOrId, String... resourceUrlParams) {
        return format("%s/%s?group-id=%s", getResourcesPath(), resourceNameOrId, groupName);
    }

    @Override
    public String getPropertiesPath(String resourceNameOrId, String... resourceUrlParams) {
        return format("%s/%s/properties?group-id=%s", getResourcesPath(), resourceNameOrId, groupName);
    }

    /**
     * Useful method for when you need to delete multiple REST API servers that point at the same modules database - set
     * the modules database to Documents for all but one, and then you can safely delete all of them.
     */
    public void setModulesDatabaseToDocuments(String serverName) {
        String payload = format("{\"server-name\":\"%s\", \"group-name\": \"%s\", \"modules-database\":\"Documents\"}",
                serverName, groupName);
        save(payload);
    }
}
