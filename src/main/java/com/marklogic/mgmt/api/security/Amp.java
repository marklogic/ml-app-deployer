package com.marklogic.mgmt.api.security;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.marklogic.mgmt.resource.ResourceManager;
import com.marklogic.mgmt.api.API;
import com.marklogic.mgmt.api.Resource;
import com.marklogic.mgmt.resource.security.AmpManager;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "role-properties")
@XmlAccessorType(XmlAccessType.FIELD)
public class Amp extends Resource {

	@JsonProperty("local-name")
	@XmlElement(name = "local-name")
    private String localName;

    private String namespace;

	@JsonProperty("document-uri")
	@XmlElement(name = "document-uri")
	private String documentUri;

	@JsonProperty("modules-database")
	@XmlElement(name = "modules-database")
	private String modulesDatabase;

	@XmlElementWrapper(name = "roles")
    private List<String> role;

    public Amp() {
        super();
    }

    public Amp(API api, String localName) {
        super(api);
        this.localName = localName;
    }

    @Override
    protected ResourceManager getResourceManager() {
        return new AmpManager(getClient());
    }

    @Override
    protected String getResourceId() {
        return localName;
    }

    @Override
    public String[] getResourceUrlParams() {
        return new String[] { "namespace", namespace, "document-uri", documentUri, "modules-database", modulesDatabase };
    }

    public void addRole(String r) {
        if (role == null) {
            role = new ArrayList<String>();
        }
        role.add(r);
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getDocumentUri() {
        return documentUri;
    }

    public void setDocumentUri(String documentUri) {
        this.documentUri = documentUri;
    }

    public String getModulesDatabase() {
        return modulesDatabase;
    }

    public void setModulesDatabase(String modulesDatabase) {
        this.modulesDatabase = modulesDatabase;
    }

    public List<String> getRole() {
        return role;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }
}
