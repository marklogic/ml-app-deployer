package com.marklogic.mgmt;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Namespace;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.marklogic.client.helper.LoggingObject;
import com.marklogic.rest.util.Fragment;
import com.marklogic.rest.util.RestTemplateUtil;

/**
 * Wraps a RestTemplate with methods that should simplify accessing the Manage API with RestTemplate. Each NounManager
 * should depend on an instance of ManageClient for accessing the Manage API.
 */
public class ManageClient extends LoggingObject {

    private ManageConfig manageConfig;
    private RestTemplate restTemplate;
    private RestTemplate adminRestTemplate;
    private String baseUrl;

    /**
     * Can use this constructor when the default values in ManageConfig will work.
     */
    public ManageClient() {
        this(new ManageConfig());
    }

    public ManageClient(ManageConfig config) {
        initialize(config);
    }

    public void initialize(ManageConfig config) {
        this.manageConfig = config;
        if (logger.isInfoEnabled()) {
            logger.info("Initializing ManageClient with manage config of: " + config);
        }
        this.restTemplate = RestTemplateUtil.newRestTemplate(config);

        if (!config.getUsername().equals(config.getAdminUsername())) {
            if (logger.isInfoEnabled()) {
                logger.info("Initializing ManageClient with admin config, admin user: " + config.getAdminUsername());
            }
            this.adminRestTemplate = RestTemplateUtil.newRestTemplate(config.getHost(), config.getPort(),
                    config.getAdminUsername(), config.getAdminPassword());
        } else {
            this.adminRestTemplate = restTemplate;
        }

        this.baseUrl = config.getBaseUrl();
        if (logger.isInfoEnabled()) {
            logger.info("Initialized ManageClient with base URL of: " + baseUrl);
        }
    }

    public ResponseEntity<String> putJson(String path, String json) {
        logRequest(path, "JSON", "PUT");
        return restTemplate.exchange(baseUrl + path, HttpMethod.PUT, buildJsonEntity(json), String.class);
    }

    public ResponseEntity<String> putJsonAsAdmin(String path, String json) {
        logAdminRequest(path, "JSON", "PUT");
        return adminRestTemplate.exchange(baseUrl + path, HttpMethod.PUT, buildJsonEntity(json), String.class);
    }

    public ResponseEntity<String> putXml(String path, String xml) {
        logRequest(path, "XML", "PUT");
        return restTemplate.exchange(baseUrl + path, HttpMethod.PUT, buildXmlEntity(xml), String.class);
    }

    public ResponseEntity<String> putXmlAsAdmin(String path, String xml) {
        logAdminRequest(path, "XML", "PUT");
        return adminRestTemplate.exchange(baseUrl + path, HttpMethod.PUT, buildXmlEntity(xml), String.class);
    }

    public ResponseEntity<String> postJson(String path, String json) {
        logRequest(path, "JSON", "POST");
        return restTemplate.exchange(baseUrl + path, HttpMethod.POST, buildJsonEntity(json), String.class);
    }

    public ResponseEntity<String> postJsonAsAdmin(String path, String json) {
        logAdminRequest(path, "JSON", "POST");
        return adminRestTemplate.exchange(baseUrl + path, HttpMethod.POST, buildJsonEntity(json), String.class);
    }

    public ResponseEntity<String> postXml(String path, String xml) {
        logRequest(path, "XML", "POST");
        return restTemplate.exchange(baseUrl + path, HttpMethod.POST, buildXmlEntity(xml), String.class);
    }

    public ResponseEntity<String> postXmlAsAdmin(String path, String xml) {
        logAdminRequest(path, "XML", "POST");
        return adminRestTemplate.exchange(baseUrl + path, HttpMethod.POST, buildXmlEntity(xml), String.class);
    }

    public ResponseEntity<String> postForm(String path, String... params) {
        logRequest(path, "form", "POST");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        for (int i = 0; i < params.length; i += 2) {
            map.add(params[i], params[i + 1]);
        }
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(map, headers);
        return restTemplate.exchange(baseUrl + path, HttpMethod.POST, entity, String.class);
    }

    public Fragment getXml(String path, String... namespacePrefixesAndUris) {
        logRequest(path, "XML", "GET");
        String xml = getRestTemplate().getForObject(getBaseUrl() + path, String.class);
        List<Namespace> list = new ArrayList<Namespace>();
        for (int i = 0; i < namespacePrefixesAndUris.length; i += 2) {
            list.add(Namespace.getNamespace(namespacePrefixesAndUris[i], namespacePrefixesAndUris[i + 1]));
        }
        return new Fragment(xml, list.toArray(new Namespace[] {}));
    }

    public Fragment getXmlAsAdmin(String path, String... namespacePrefixesAndUris) {
        logAdminRequest(path, "XML", "GET");
        String xml = getAdminRestTemplate().getForObject(getBaseUrl() + path, String.class);
        List<Namespace> list = new ArrayList<Namespace>();
        for (int i = 0; i < namespacePrefixesAndUris.length; i += 2) {
            list.add(Namespace.getNamespace(namespacePrefixesAndUris[i], namespacePrefixesAndUris[i + 1]));
        }
        return new Fragment(xml, list.toArray(new Namespace[] {}));
    }

    public String getJson(String path) {
        logRequest(path, "JSON", "GET");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        return getRestTemplate().exchange(getBaseUrl() + path, HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getBody();
    }

    public void delete(String path) {
        logRequest(path, "", "DELETE");
        restTemplate.delete(baseUrl + path);
    }

    public void deleteAsAdmin(String path) {
        logAdminRequest(path, "", "DELETE");
        adminRestTemplate.delete(baseUrl + path);
    }

    public HttpEntity<String> buildJsonEntity(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<String>(json, headers);
    }

    public HttpEntity<String> buildXmlEntity(String xml) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        return new HttpEntity<String>(xml, headers);
    }

    protected void logRequest(String path, String contentType, String method) {
        if (logger.isInfoEnabled()) {
            logger.info(String.format("Sending %s %s request as user '%s' to path: %s", contentType, method,
                    manageConfig.getUsername(), path));
        }
    }

    protected void logAdminRequest(String path, String contentType, String method) {
        if (logger.isInfoEnabled()) {
            logger.info(String.format("Sending %s %s request as user with admin role '%s' to path: %s", contentType,
                    method, manageConfig.getUsername(), path));
        }
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public RestTemplate getAdminRestTemplate() {
        return adminRestTemplate;
    }

    public ManageConfig getManageConfig() {
        return manageConfig;
    }
}
