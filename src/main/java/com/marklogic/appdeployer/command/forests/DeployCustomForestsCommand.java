package com.marklogic.appdeployer.command.forests;

import com.marklogic.appdeployer.command.AbstractCommand;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.SortOrderConstants;

import java.io.File;

/**
 * For each directory in the ml-config/forests directory, TODO.
 */
public class DeployCustomForestsCommand extends AbstractCommand {

	public DeployCustomForestsCommand() {
		setExecuteSortOrder(SortOrderConstants.DEPLOY_FORESTS);
	}

	@Override
	public void execute(CommandContext context) {
		File dir = new File(context.getAppConfig().getConfigDir().getBaseDir(), "forests");
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				processDirectory(f, context);
			}
		}
	}

	protected void processDirectory(File dir, CommandContext context) {
		for (File f : listFilesInDirectory(dir)) {
			logger.info("Processing: " + f.getAbsolutePath());
		}

	}
}
