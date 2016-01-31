package com.marklogic.mgmt.groups;

import com.marklogic.mgmt.AbstractResourceManager;
import com.marklogic.mgmt.DeleteReceipt;
import com.marklogic.mgmt.ManageClient;

public class GroupManager extends AbstractResourceManager {

    public GroupManager(ManageClient manageClient) {
        super(manageClient);
    }

    @Override
    protected boolean useAdminUser() {
        return true;
    }

    @Override
    public DeleteReceipt delete(String payload, String... resourceUrlParams) {
        String resourceId = getResourceId(payload);
        if (resourceId != null && resourceId.toUpperCase().equals("DEFAULT")) {
            return new DeleteReceipt(resourceId, null, false);
        }
        return super.delete(payload);
    }
}
