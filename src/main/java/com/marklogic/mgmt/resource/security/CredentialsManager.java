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
package com.marklogic.mgmt.resource.security;

import com.marklogic.mgmt.ManageClient;
import com.marklogic.mgmt.SaveReceipt;
import com.marklogic.mgmt.resource.AbstractResourceManager;
import org.springframework.http.ResponseEntity;

public class CredentialsManager extends AbstractResourceManager {
	public CredentialsManager(ManageClient client) {
		super(client);
	}

	@Override
	public String getResourcesPath() {
		return "/manage/v2/credentials/properties";
	}

	@Override
	protected String getCreateResourcePath(String payload) {
		if (payloadParser.isJsonPayload(payload)) {
			return getResourcesPath() + "?format=json";
		} else {
			return getResourcesPath() + "?format=xml";
		}
	}

	@Override
	protected String getIdFieldName() {
		return "type";
	}

	@Override
	protected String getResourceId(String payload) {
		if (payloadParser.isJsonPayload(payload)) {
			return payloadParser.getPayloadFieldValue(payload, getIdFieldName());
		} else {
			if (payloadParser.getPayloadFieldValue(payload, "aws", false) != null) {
				return "aws";
			} else if (payloadParser.getPayloadFieldValue(payload, "azure", false) != null) {
				return "azure";
			} else {
				return null;
			}
		}
	}

	@Override
	protected SaveReceipt createNewResource(String payload, String resourceId) {
		String label = getResourceName();
		if (logger.isInfoEnabled()) {
			logger.info(format("Creating %s: %s", label, resourceId));
		}
		String path = getCreateResourcePath(payload);
		ResponseEntity<String> response = putPayload(getManageClient(), path, payload);
		if (logger.isInfoEnabled()) {
			logger.info(format("Created %s: %s", label, resourceId));
		}
		return new SaveReceipt(resourceId, payload, path, response);
	}
}
