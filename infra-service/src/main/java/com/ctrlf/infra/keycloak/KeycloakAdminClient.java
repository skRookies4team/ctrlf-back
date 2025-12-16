package com.ctrlf.infra.keycloak;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.net.URI;

@Component
public class KeycloakAdminClient {

    private final KeycloakAdminProperties props;
    private final RestTemplate restTemplate = new RestTemplate();

    public KeycloakAdminClient(KeycloakAdminProperties props) {
        this.props = props;
    }

    private String tokenEndpoint() {
        return props.getBaseUrl() + "/realms/" + props.getRealm() + "/protocol/openid-connect/token";
    }

    private String adminApi(String path) {
        return props.getBaseUrl() + "/admin/realms/" + props.getRealm() + path;
    }

    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", props.getClientId());
        form.add("client_secret", props.getClientSecret());
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
        Map<?, ?> resp = restTemplate.postForObject(tokenEndpoint(), entity, Map.class);
        if (resp == null || !resp.containsKey("access_token")) {
            throw new IllegalStateException("Failed to obtain Keycloak access token");
        }
        return String.valueOf(resp.get("access_token"));
    }

    public List<Map<String, Object>> listUsers(String search, int first, int max) {
        String url = adminApi("/users?first=" + first + "&max=" + max + (search != null && !search.isBlank() ? "&search=" + search : ""));
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<Void> req = new HttpEntity<>(headers);
        ResponseEntity<List> resp = restTemplate.exchange(url, HttpMethod.GET, req, List.class);
        return resp.getBody();
    }

    public String createUser(Map<String, Object> payload, String initialPassword, boolean temporary) {
        String url = adminApi("/users");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);
        ResponseEntity<Void> resp = restTemplate.postForEntity(url, req, Void.class);
        URI loc = resp.getHeaders().getLocation();
        String userId = null;
        if (loc != null) {
            String path = loc.getPath();
            int idx = path.lastIndexOf('/');
            if (idx >= 0 && idx < path.length()-1) {
                userId = path.substring(idx+1);
            }
        }
        if (userId == null) {
            throw new IllegalStateException("Failed to parse created user id from Location header");
        }
        if (initialPassword != null && !initialPassword.isBlank()) {
            resetPassword(userId, initialPassword, temporary);
        }
        return userId;
    }

    public void updateUser(String userId, Map<String, Object> payload) {
        String url = adminApi("/users/" + userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);
        restTemplate.exchange(url, HttpMethod.PUT, req, Void.class);
    }

    public void resetPassword(String userId, String newPassword, boolean temporary) {
        String url = adminApi("/users/" + userId + "/reset-password");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> payload = Map.of(
            "type", "password",
            "value", newPassword,
            "temporary", temporary
        );
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);
        restTemplate.exchange(url, HttpMethod.PUT, req, Void.class);
    }

    public Map<String, Object> getUser(String userId) {
        String url = adminApi("/users/" + userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<Void> req = new HttpEntity<>(headers);
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, req, Map.class);
        return resp.getBody();
    }

    private String userInfoEndpoint() {
        return props.getBaseUrl() + "/realms/" + props.getRealm() + "/protocol/openid-connect/userinfo";
    }

    private String introspectEndpoint() {
        return props.getBaseUrl() + "/realms/" + props.getRealm() + "/protocol/openid-connect/token/introspect";
    }

    /**
     * 주어진 액세스 토큰에 해당하는 사용자 정보를 조회합니다.
     * Authorization: Bearer {accessToken} 으로 userinfo 엔드포인트를 호출합니다.
     */
    public Map<String, Object> getUserInfoWithToken(String accessToken) {
        String token = accessToken;
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("accessToken is required");
        }
        if (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> req = new HttpEntity<>(headers);
        ResponseEntity<Map> resp = restTemplate.exchange(userInfoEndpoint(), HttpMethod.GET, req, Map.class);
        return resp.getBody();
    }

    /**
     * 토큰 인트로스펙션을 수행하여 토큰의 활성 여부 및 클레임을 반환합니다.
     * clientId/secret로 인증하여 { active, client_id, username, scope, sub, exp ... } 등을 반환합니다.
     */
    public Map<String, Object> introspectToken(String accessToken) {
        String token = accessToken;
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("accessToken is required");
        }
        if (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("token", token);
        // client 인증 (confidential)
        form.add("client_id", props.getClientId());
        if (props.getClientSecret() != null && !props.getClientSecret().isBlank()) {
            form.add("client_secret", props.getClientSecret());
        }
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
        Map body = restTemplate.postForObject(introspectEndpoint(), entity, Map.class);
        return body;
    }
}


