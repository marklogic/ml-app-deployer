package com.marklogic.rest.mgmt.admin;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.marklogic.rest.mgmt.AbstractManager;
import com.marklogic.rest.util.RestTemplateUtil;

public class AdminManager extends AbstractManager {

    private int waitForRestartCheckInterval = 1000;
    private RestTemplate restTemplate;
    private AdminConfig adminConfig;

    /**
     * Can use this constructor when the default values in ManageConfig will work.
     */
    public AdminManager() {
        this(new AdminConfig());
    }

    public AdminManager(AdminConfig adminConfig) {
        this.adminConfig = adminConfig;
        this.restTemplate = RestTemplateUtil.newRestTemplate(adminConfig);
    }

    public void init() {
        init(null, null);
    }

    public void init(String licenseKey, String licensee) {
        final String url = adminConfig.getBaseUrl() + "/admin/v1/init";

        String json = null;
        if (licenseKey != null && licensee != null) {
            json = format("{\"license-key\":\"%s\", \"licensee\":\"%s\"}", licenseKey, licensee);
        }
        final String payload = json;

        logger.info("Initializing MarkLogic at: " + url);
        invokeActionRequiringRestart(new ActionRequiringRestart() {
            @Override
            public boolean execute() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<String>(payload, headers);
                try {
                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                    logger.info("Initialization response: " + response);
                    // According to http://docs.marklogic.com/REST/POST/admin/v1/init, a 202 is sent back in the event a
                    // restart is needed. A 400 or 401 will be thrown as an error by RestTemplate.
                    return HttpStatus.ACCEPTED.equals(response.getStatusCode());
                } catch (HttpClientErrorException hcee) {
                    String body = hcee.getResponseBodyAsString();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Response body: " + body);
                    }
                    if (body != null && body.contains("MANAGE-ALREADYINIT")) {
                        logger.info("MarkLogic has already been initialized");
                        return false;
                    } else {
                        logger.error("Caught error, response body: " + body);
                        throw hcee;
                    }
                }
            }
        });
    }

    public void installAdmin() {
        installAdmin(null, null);
    }

    public void installAdmin(String username, String password) {
        final String url = adminConfig.getBaseUrl() + "/admin/v1/instance-admin";

        String json = null;
        if (username != null && password != null) {
            json = format("{\"admin-username\":\"%s\", \"admin-password\":\"%s\", \"realm\":\"public\"}", username,
                    password);
        }
        final String payload = json;

        logger.info("Installing admin user at: " + url);
        invokeActionRequiringRestart(new ActionRequiringRestart() {
            @Override
            public boolean execute() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<String>(payload, headers);
                try {
                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                    logger.info("Admin installation response: " + response);
                    // According to http://docs.marklogic.com/REST/POST/admin/v1/init, a 202 is sent back in the event a
                    // restart is needed. A 400 or 401 will be thrown as an error by RestTemplate.
                    return HttpStatus.ACCEPTED.equals(response.getStatusCode());
                } catch (HttpClientErrorException hcee) {
                    if (HttpStatus.BAD_REQUEST.equals(hcee.getStatusCode())) {
                        logger.warn("Caught 400 error, assuming admin user already installed; response body: "
                                + hcee.getResponseBodyAsString());
                        return false;
                    }
                    throw hcee;
                }
            }
        });
    }

    /**
     * This used to be much more complex - the code first got the latest restart timestamp and then waited for a new
     * value. But based on the "delay()" method implementation in marklogic-samplestack, we can just keep catching
     * exceptions until the call to get the restart timestamp works.
     * 
     * @param action
     */
    public void invokeActionRequiringRestart(ActionRequiringRestart action) {
        boolean requiresRestart = action.execute();
        if (requiresRestart) {
            waitForRestart();
        }
    }

    public String getLastRestartTimestamp() {
        return restTemplate.getForEntity(adminConfig.getBaseUrl() + "/admin/v1/timestamp", String.class).getBody();
    }

    public void waitForRestart() {
        try {
            Thread.sleep(waitForRestartCheckInterval);
            getLastRestartTimestamp();
        } catch (Exception ex) {
            logger.info("Waiting for MarkLogic to restart...");
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception while waiting for MarkLogic to restart: " + ex.getMessage(), ex);
            }
            waitForRestart();
        }
    }

    public void setWaitForRestartCheckInterval(int waitForRestartCheckInterval) {
        this.waitForRestartCheckInterval = waitForRestartCheckInterval;
    }

}
