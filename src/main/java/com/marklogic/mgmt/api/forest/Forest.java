package com.marklogic.mgmt.api.forest;

import java.util.List;

import com.marklogic.mgmt.ResourceManager;
import com.marklogic.mgmt.api.API;
import com.marklogic.mgmt.api.Resource;
import com.marklogic.mgmt.api.database.DatabaseReplication;
import com.marklogic.mgmt.forests.ForestManager;
import com.marklogic.mgmt.hosts.HostManager;

public class Forest extends Resource {

    private String forestName;
    private Boolean enabled;
    private String host;
    private String dataDirectory;
    private String largeDataDirectory;
    private String fastDataDirectory;
    private String updatesAllowed;
    private String availability;
    private Boolean rebalancerEnable;
    private List<Range> range;
    private Boolean failoverEnable;
    private List<String> failoverHost;
    private List<ForestBackup> forestBackup;
    private List<DatabaseReplication> databaseReplication;

    public Forest() {
    }

    public Forest(API api, String forestName) {
        super(api);
        setForestName(forestName);
    }

    @Override
    protected String getResourceLabel() {
        return getForestName();
    }

    @Override
    protected ResourceManager getResourceManager() {
        return new ForestManager(getClient());
    }

    @Override
    protected String getResourceId() {
        return forestName;
    }

    /**
     * save is tricky for forests, because many of the properties are read-only, and thus ForestManager does not yet
     * support updates.
     * 
     * Another tricky part is that "localhost" won't work as a hostname - it has to be the real hostname. So if it's not
     * set, we have to fetch it from the cluster.
     */
    @Override
    public String save() {
        if (host == null) {
            String host = new HostManager(getClient()).getHostNames().get(0);
            if (logger.isInfoEnabled()) {
                logger.info(format("Setting forest host to %s", host));
            }
            this.host = host;
        }
        return super.save();
    }

    public String getForestName() {
        return forestName;
    }

    public void setForestName(String forestName) {
        this.forestName = forestName;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public String getLargeDataDirectory() {
        return largeDataDirectory;
    }

    public void setLargeDataDirectory(String largeDataDirectory) {
        this.largeDataDirectory = largeDataDirectory;
    }

    public String getFastDataDirectory() {
        return fastDataDirectory;
    }

    public void setFastDataDirectory(String fastDataDirectory) {
        this.fastDataDirectory = fastDataDirectory;
    }

    public String getUpdatesAllowed() {
        return updatesAllowed;
    }

    public void setUpdatesAllowed(String updatesAllowed) {
        this.updatesAllowed = updatesAllowed;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public Boolean isRebalancerEnable() {
        return rebalancerEnable;
    }

    public void setRebalancerEnable(Boolean rebalancerEnable) {
        this.rebalancerEnable = rebalancerEnable;
    }

    public List<Range> getRange() {
        return range;
    }

    public void setRange(List<Range> range) {
        this.range = range;
    }

    public Boolean isFailoverEnable() {
        return failoverEnable;
    }

    public void setFailoverEnable(Boolean failoverEnable) {
        this.failoverEnable = failoverEnable;
    }

    public List<String> getFailoverHost() {
        return failoverHost;
    }

    public void setFailoverHost(List<String> failoverHost) {
        this.failoverHost = failoverHost;
    }

    public List<ForestBackup> getForestBackup() {
        return forestBackup;
    }

    public void setForestBackup(List<ForestBackup> forestBackup) {
        this.forestBackup = forestBackup;
    }

    public List<DatabaseReplication> getDatabaseReplication() {
        return databaseReplication;
    }

    public void setDatabaseReplication(List<DatabaseReplication> databaseReplication) {
        this.databaseReplication = databaseReplication;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Boolean getRebalancerEnable() {
        return rebalancerEnable;
    }

    public Boolean getFailoverEnable() {
        return failoverEnable;
    }
}
