package com.marklogic.appdeployer.command;

import java.io.File;
import java.util.Date;

/**
 * Defines operations for managing whether a module needs to be installed or not.
 */
public interface ResourceManager {

    /**
     * Give the implementor a chance to initialize itself - e.g. loading data from a properties file or other resource.
     */
    void initialize();

    boolean hasFileBeenModifiedSinceLastDeployed(File file);

    void saveLastDeployedHash(File file);
}
