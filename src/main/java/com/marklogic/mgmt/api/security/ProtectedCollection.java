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
package com.marklogic.mgmt.api.security;

import com.marklogic.mgmt.resource.ResourceManager;
import com.marklogic.mgmt.api.API;
import com.marklogic.mgmt.api.Resource;
import com.marklogic.mgmt.resource.security.ProtectedCollectionsManager;

import java.util.ArrayList;
import java.util.List;

public class ProtectedCollection extends Resource {

    private String collection;
    private List<Permission> permission;

    public ProtectedCollection() {
        super();
    }

    public ProtectedCollection(API api, String collection) {
        super(api);
        this.collection = collection;
    }

    public void addPermission(Permission p) {
        if (permission == null) {
            permission = new ArrayList<>();
        }
        permission.add(p);
    }

    @Override
    protected ResourceManager getResourceManager() {
        return new ProtectedCollectionsManager(getClient());
    }

    @Override
    protected String getResourceId() {
        return collection;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public List<Permission> getPermission() {
        return permission;
    }

    public void setPermission(List<Permission> permission) {
        this.permission = permission;
    }

}
