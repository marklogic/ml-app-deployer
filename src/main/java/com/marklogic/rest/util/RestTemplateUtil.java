package com.marklogic.rest.util;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RestTemplateUtil {

    public static RestTemplate newRestTemplate(RestConfig config) {
        return newRestTemplate(config.getHost(), config.getPort(), config.getUsername(), config.getPassword());
    }

    public static RestTemplate newRestTemplate(String host, int port, String username, String password) {
        BasicCredentialsProvider prov = new BasicCredentialsProvider();
        prov.setCredentials(new AuthScope(host, port, AuthScope.ANY_REALM), new UsernamePasswordCredentials(username,
                password));
        HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(prov).build();
        RestTemplate rt = new RestTemplate(new HttpComponentsClientHttpRequestFactory(client));
        rt.setErrorHandler(new MgmtResponseErrorHandler());
        return rt;
    }
}
