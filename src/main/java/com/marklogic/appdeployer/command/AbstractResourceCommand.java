package com.marklogic.appdeployer.command;

import java.io.File;

import com.marklogic.mgmt.ResourceManager;
import com.marklogic.mgmt.SaveReceipt;
import com.marklogic.mgmt.admin.ActionRequiringRestart;

/**
 * Provides a basic implementation for creating/updating a resource while an app is being deployed and then deleting it
 * while the app is being undeployed.
 */
public abstract class AbstractResourceCommand extends AbstractUndoableCommand {

    private boolean deleteResourcesOnUndo = true;
    private boolean restartAfterDelete = false;
    private boolean catchExceptionOnDeleteFailure = false;

    protected abstract File[] getResourceDirs(CommandContext context);

    protected abstract ResourceManager getResourceManager(CommandContext context);

    @Override
    public void execute(CommandContext context) {
        for (File resourceDir : getResourceDirs(context)) {
            if (resourceDir.exists()) {
                ResourceManager mgr = getResourceManager(context);
                if (logger.isInfoEnabled()) {
                    logger.info("Processing files in directory: " + resourceDir.getAbsolutePath());
                }
                for (File f : listFilesInDirectory(resourceDir)) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Processing file: " + f.getAbsolutePath());
                    }
                    SaveReceipt receipt = saveResource(mgr, context, f);
                    afterResourceSaved(mgr, context, f, receipt);
                }
            }
        }
    }

    /**
     * Subclasses can override this to add functionality after a resource has been saved.
     *
     * @param mgr
     * @param context
     * @param resourceFile
     * @param receipt
     */
    protected void afterResourceSaved(ResourceManager mgr, CommandContext context, File resourceFile,
            SaveReceipt receipt) {

    }

    @Override
    public void undo(CommandContext context) {
        if (deleteResourcesOnUndo) {
            for (File resourceDir : getResourceDirs(context)) {
                if (resourceDir.exists()) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Processing files in directory: " + resourceDir.getAbsolutePath());
                    }
                    final ResourceManager mgr = getResourceManager(context);
                    for (File f : listFilesInDirectory(resourceDir)) {
                        if (logger.isInfoEnabled()) {
                            logger.info("Processing file: " + f.getAbsolutePath());
                        }
                        deleteResource(mgr, context, f);
                    }
                }
            }
        }
    }

    /**
     * If catchExceptionOnDeleteFailure is set to true, this will catch and log any exception that occurs when trying to
     * delete the resource. This has been necessary when deleting two app servers in a row - for some reason, the 2nd
     * delete will intermittently fail with a connection reset error, but the app server is in fact deleted
     * successfully.
     *
     * @param mgr
     * @param context
     * @param f
     */
    protected void deleteResource(final ResourceManager mgr, CommandContext context, File f) {
        final String payload = copyFileToString(f, context);
        try {
            if (restartAfterDelete) {
                context.getAdminManager().invokeActionRequiringRestart(new ActionRequiringRestart() {
                    @Override
                    public boolean execute() {
                        return mgr.delete(payload).isDeleted();
                    }
                });
            } else {
                mgr.delete(payload);
            }
        } catch (RuntimeException e) {
            if (catchExceptionOnDeleteFailure) {
                logger.warn("Caught exception while trying to delete resource; cause: " + e.getMessage());
                if (restartAfterDelete) {
                    context.getAdminManager().waitForRestart();
                }
            } else {
                throw e;
            }
        }
    }

    public void setDeleteResourcesOnUndo(boolean deleteResourceOnUndo) {
        this.deleteResourcesOnUndo = deleteResourceOnUndo;
    }

    public void setRestartAfterDelete(boolean restartAfterDelete) {
        this.restartAfterDelete = restartAfterDelete;
    }

    public boolean isDeleteResourcesOnUndo() {
        return deleteResourcesOnUndo;
    }

    public boolean isRestartAfterDelete() {
        return restartAfterDelete;
    }

    public void setCatchExceptionOnDeleteFailure(boolean catchExceptionOnDeleteFailure) {
        this.catchExceptionOnDeleteFailure = catchExceptionOnDeleteFailure;
    }
}
