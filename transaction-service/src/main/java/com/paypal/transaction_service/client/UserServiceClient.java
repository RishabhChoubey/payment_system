package com.paypal.transaction_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import java.util.Map;

@Component
public class UserServiceClient {
    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    public UserServiceClient(@Value("${user.service.base-url:http://localhost:8081}") String userServiceBaseUrl) {
        this.userServiceBaseUrl = userServiceBaseUrl;
        this.restTemplate = new RestTemplate();
        this.restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(userServiceBaseUrl));
    }

    public Long getCurrentUserId(String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/users/me", HttpMethod.GET, entity, Map.class
        );
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Object id = response.getBody().get("id");
            if (id instanceof Integer) {
                return ((Integer) id).longValue();
            } else if (id instanceof Long) {
                return (Long) id;
            } else if (id instanceof String) {
                return Long.parseLong((String) id);
            }
        }
        throw new RuntimeException("Failed to get user id from user service");
    }

    public Long getUserIdByEmail(String email, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        // Assuming user-service exposes /api/users/email/{email} returning user object with id
        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/users/email/{email}", HttpMethod.GET, entity, Map.class, email
        );
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Object id = response.getBody().get("id");
            if (id instanceof Integer) {
                return ((Integer) id).longValue();
            } else if (id instanceof Long) {
                return (Long) id;
            } else if (id instanceof String) {
                return Long.parseLong((String) id);
            }
        }
        throw new RuntimeException("Failed to get user id by email from user service");
    }
}
