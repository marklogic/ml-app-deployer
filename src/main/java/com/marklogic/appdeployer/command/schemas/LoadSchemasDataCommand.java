package com.marklogic.appdeployer.command.schemas;

import java.io.File;

import com.marklogic.appdeployer.AppConfig;
import com.marklogic.appdeployer.command.AbstractCommand;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.SortOrderConstants;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.dataloader.SchemasDataLoader;
import com.marklogic.client.dataloader.impl.DefaultSchemasDataFinder;
import com.marklogic.client.dataloader.impl.DefaultSchemasDataLoader;

public class LoadSchemasDataCommand extends AbstractCommand  {

	private SchemasDataLoader schemasDataLoader;
	
	public LoadSchemasDataCommand() {
        setExecuteSortOrder(SortOrderConstants.DEPLOY_SQL_VIEWS);
	}
	
	@Override
	public void execute(CommandContext context) {
		loadSchemasDataIntoSchemasDatabase(context);
	}

	protected void loadSchemasDataIntoSchemasDatabase(CommandContext context) {
		if (schemasDataLoader == null) {
			initializeDefaultSchemasDataLoader(context);
		}
		
		
		AppConfig config = context.getAppConfig();
        DatabaseClient client = config.newSchemasDatabaseClient();
        try {
        	File schemasDataDir = config.getConfigDir().getBaseDir();
                
        	logger.info("Loading schemas database data from dir: " + schemasDataDir);
        	schemasDataLoader.loadSchemasData(schemasDataDir, new DefaultSchemasDataFinder(), client);
            
        } finally {
            client.release();
        }
	}

	private void initializeDefaultSchemasDataLoader(CommandContext context) {
	     logger.info("Initializing instance of DefaultSchemasLoader");
	     this.schemasDataLoader = new DefaultSchemasDataLoader();
	}

}
