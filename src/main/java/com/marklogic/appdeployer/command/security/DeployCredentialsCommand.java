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
package com.marklogic.appdeployer.command.security;

import com.marklogic.appdeployer.command.AbstractResourceCommand;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.SortOrderConstants;
import com.marklogic.appdeployer.command.UndoableCommand;
import com.marklogic.mgmt.ManageClient;
import com.marklogic.mgmt.PayloadParser;
import com.marklogic.mgmt.resource.ResourceManager;
import com.marklogic.mgmt.resource.security.CredentialsManager;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.io.File;
import java.net.URI;

public class DeployCredentialsCommand extends AbstractResourceCommand implements UndoableCommand {

	public DeployCredentialsCommand() {
		setExecuteSortOrder(SortOrderConstants.DEPLOY_CREDENTIALS);
		setUndoSortOrder(SortOrderConstants.DEPLOY_CREDENTIALS);
	}

	@Override
	protected File[] getResourceDirs(CommandContext context) {
		return findResourceDirs(context, configDir -> configDir.getCredentialsDir());
	}

	@Override
	protected ResourceManager getResourceManager(CommandContext context) {
		return new CredentialsManager(context.getManageClient());
	}

	@Override
	protected void deleteResource(ResourceManager mgr, CommandContext context, File f) {
		String payload = copyFileToString(f, context);
		String type = "aws";

		if (new PayloadParser().isJsonPayload(payload)) {
			if (payload.contains("\"azure\"")) {
				type = "azure";
			}
		} else {
			if (payload.contains("<azure>")) {
				type = "azure";
			}
		}

		ManageClient manageClient = context.getManageClient();
		URI credentialsURI = manageClient.buildUri((new CredentialsManager(manageClient)).getResourcesPath() + "?type=" + type);
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-type", "application/json");
		HttpEntity<Resource> resourceEntity = new HttpEntity<>(null, headers);

		manageClient.getRestTemplate().exchange(credentialsURI, HttpMethod.DELETE, resourceEntity, String.class);
	}

}
