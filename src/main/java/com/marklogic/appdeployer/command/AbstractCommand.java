package com.marklogic.appdeployer.command;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import org.springframework.util.FileCopyUtils;

import com.marklogic.client.helper.LoggingObject;
import com.marklogic.mgmt.ResourceManager;
import com.marklogic.mgmt.SaveReceipt;

/**
 * Abstract base class that provides some convenience methods for implementing a command. Subclasses will typically
 * override the default sort order within the subclass constructor.
 */
public abstract class AbstractCommand extends LoggingObject implements Command {

    private int executeSortOrder = Integer.MAX_VALUE;
    private boolean storeResourceIdsAsCustomTokens = false;

    protected TokenReplacer tokenReplacer = new DefaultTokenReplacer();
    private FilenameFilter resourceFilenameFilter = new ResourceFilenameFilter();

    /**
     * A subclass can set the executeSortOrder attribute to whatever value it needs.
     */
    @Override
    public Integer getExecuteSortOrder() {
        return this.executeSortOrder;
    }

    /**
     * Simplifies reading the contents of a File into a String.
     * 
     * @param f
     * @return
     */
    protected String copyFileToString(File f) {
        try {
            return new String(FileCopyUtils.copyToByteArray(f));
        } catch (IOException ie) {
            throw new RuntimeException("Unable to copy file to string from path: " + f.getAbsolutePath() + "; cause: "
                    + ie.getMessage(), ie);
        }
    }

    /**
     * Provides a basic implementation for saving a resource defined in a File, including replacing tokens.
     * 
     * @param mgr
     * @param context
     * @param f
     * @return
     */
    protected SaveReceipt saveResource(ResourceManager mgr, CommandContext context, File f) {
        String payload = copyFileToString(f);
        payload = tokenReplacer.replaceTokens(payload, context.getAppConfig(), false);
        SaveReceipt receipt = mgr.save(payload);
        if (storeResourceIdsAsCustomTokens) {
            storeTokenForResourceId(receipt, context);
        }
        return receipt;
    }

    /**
     * Any resource that may be referenced by its ID by another resource will most likely need its ID stored as a custom
     * token so that it can be referenced by the other resource. To enable this, the subclass should set
     * storeResourceIdAsCustomToken to true.
     * 
     * @param receipt
     * @param context
     */
    protected void storeTokenForResourceId(SaveReceipt receipt, CommandContext context) {
        URI location = receipt.getResponse().getHeaders().getLocation();

        String idValue = null;
        String resourceName = null;

        if (location != null) {
            String[] tokens = location.getPath().split("/");
            idValue = tokens[tokens.length - 1];
            resourceName = tokens[tokens.length - 2];
        } else {
            String[] tokens = receipt.getPath().split("/");
            // Path is expected to end in /(resources-name)/(id)/properties
            idValue = tokens[tokens.length - 2];
            resourceName = tokens[tokens.length - 3];
        }

        String key = "%%" + resourceName + "-id-" + receipt.getResourceId() + "%%";
        if (logger.isInfoEnabled()) {
            logger.info(format("Storing token with key '%s' and value '%s'", key, idValue));
        }

        context.getAppConfig().getCustomTokens().put(key, idValue);
    }

    protected File[] listFilesInDirectory(File dir) {
        File[] files = dir.listFiles(resourceFilenameFilter);
        Arrays.sort(files);
        return files;
    }

    public void setTokenReplacer(TokenReplacer tokenReplacer) {
        this.tokenReplacer = tokenReplacer;
    }

    public void setExecuteSortOrder(int executeSortOrder) {
        this.executeSortOrder = executeSortOrder;
    }

    public void setStoreResourceIdsAsCustomTokens(boolean storeResourceIdsAsCustomTokens) {
        this.storeResourceIdsAsCustomTokens = storeResourceIdsAsCustomTokens;
    }

    public void setResourceFilenameFilter(FilenameFilter resourceFilenameFilter) {
        this.resourceFilenameFilter = resourceFilenameFilter;
    }
}
