/*
 * Copyright (c) 2023 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.appdeployer.export.cpf;

import com.marklogic.appdeployer.ConfigDir;
import com.marklogic.appdeployer.export.impl.AbstractNamedResourceExporter;
import com.marklogic.appdeployer.export.impl.ExportInputs;
import com.marklogic.mgmt.ManageClient;
import com.marklogic.mgmt.resource.ResourceManager;
import com.marklogic.mgmt.resource.cpf.PipelineManager;

import java.io.File;

public class PipelineExporter extends AbstractNamedResourceExporter {

	private String databaseIdOrName;

	public PipelineExporter(ManageClient manageClient, String databaseIdOrName, String... resourceNames) {
		super(manageClient, resourceNames);
		this.databaseIdOrName = databaseIdOrName;
	}

	@Override
	protected String[] getExportMessages() {
		return new String[]{"Each exported CPF pipeline has the 'pipeline-id' field removed from it, as that field should be generated by MarkLogic."};
	}

	@Override
	protected String beforeResourceWrittenToFile(ExportInputs exportInputs, String payload) {
		return removeJsonKeyFromPayload(payload, "pipeline-id");
	}

	@Override
	protected ResourceManager newResourceManager(ManageClient manageClient) {
		return new PipelineManager(manageClient, databaseIdOrName);
	}

	@Override
	protected File getResourceDirectory(File baseDir) {
		return new ConfigDir(baseDir).getPipelinesDir();
	}
}
