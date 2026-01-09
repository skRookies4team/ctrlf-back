package com.ctrlf.infra.config;

import com.ctrlf.infra.keycloak.KeycloakAdminProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Keycloak Service Account 권한 자동 설정 Runner
 * 
 * infra-admin 클라이언트의 Service Account에 realm-management 클라이언트의
 * view-users, manage-users, view-realm 역할을 자동으로 할당합니다.
 * 
 * 활성화: --spring.profiles.active=local,keycloak-setup
 * 또는 application.yml에서 app.keycloak.auto-setup.enabled=true로 설정
 */
@Profile("keycloak-setup")
@Order(0) // 다른 Runner보다 먼저 실행
@Component
public class KeycloakServiceAccountSetupRunner implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(KeycloakServiceAccountSetupRunner.class);
    
    private final KeycloakAdminProperties props;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String REALM_MANAGEMENT_CLIENT = "realm-management";
    // 필요한 역할 목록
    // - view-users: 사용자 조회
    // - manage-users: 사용자 생성/수정/삭제
    // - view-realm: realm 정보 조회
    // - realm-admin: realm 전체 관리 권한 (모든 realm-management 권한 포함, 선택사항)
    // private static final List<String> REQUIRED_ROLES = Arrays.asList("view-users", "manage-users", "view-realm");
    // realm-admin을 포함하려면 아래처럼 변경:
    private static final List<String> REQUIRED_ROLES = Arrays.asList("view-users", "manage-users", "view-realm", "realm-admin");
    
    public KeycloakServiceAccountSetupRunner(KeycloakAdminProperties props) {
        this.props = props;
    }
    
    @Override
    public void run(String... args) {
        log.info("🔐 Keycloak Service Account 권한 자동 설정 시작...");
        log.info("   Keycloak URL: {}", props.getBaseUrl());
        log.info("   Realm: {}", props.getRealm());
        log.info("   Client: {}", props.getClientId());
        
        try {
            // 1. 관리자 토큰 획득
            String adminToken = getAdminToken();
            if (adminToken == null) {
                log.warn("⚠️  관리자 토큰 획득 실패. Keycloak Admin Console에서 수동으로 설정하세요.");
                return;
            }
            
            // 2. infra-admin 클라이언트 UUID 조회
            String clientUuid = getClientUuid(adminToken, props.getClientId());
            if (clientUuid == null) {
                log.warn("⚠️  클라이언트 '{}'를 찾을 수 없습니다.", props.getClientId());
                return;
            }
            
            // 3. Service Account 사용자 ID 조회
            String serviceAccountUserId = getServiceAccountUserId(adminToken, clientUuid);
            if (serviceAccountUserId == null) {
                log.warn("⚠️  Service Account 사용자를 찾을 수 없습니다.");
                return;
            }
            
            // 4. realm-management 클라이언트 UUID 조회
            String realmMgmtClientUuid = getClientUuid(adminToken, REALM_MANAGEMENT_CLIENT);
            if (realmMgmtClientUuid == null) {
                log.warn("⚠️  'realm-management' 클라이언트를 찾을 수 없습니다.");
                return;
            }
            
            // 5. 현재 할당된 역할 조회
            List<Map<String, Object>> currentRoles = getCurrentRoles(adminToken, serviceAccountUserId, realmMgmtClientUuid);
            
            // 6. 필요한 역할 할당
            int assignedCount = 0;
            for (String roleName : REQUIRED_ROLES) {
                if (isRoleAssigned(currentRoles, roleName)) {
                    log.info("   ✅ 역할 '{}'은 이미 할당되어 있습니다.", roleName);
                    continue;
                }
                
                if (assignRole(adminToken, serviceAccountUserId, realmMgmtClientUuid, roleName)) {
                    log.info("   ✅ 역할 '{}' 할당 완료", roleName);
                    assignedCount++;
                } else {
                    log.warn("   ⚠️  역할 '{}' 할당 실패", roleName);
                }
            }
            
            log.info("🎉 설정 완료! {} 개의 역할이 할당되었습니다.", assignedCount);
            log.info("📝 할당된 역할: {}", REQUIRED_ROLES);
            
        } catch (Exception e) {
            log.error("❌ Keycloak Service Account 권한 설정 중 오류 발생", e);
        }
    }
    
    private String getAdminToken() {
        try {
            String url = props.getBaseUrl() + "/realms/master/protocol/openid-connect/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("username", "admin");
            form.add("password", "admin");
            form.add("grant_type", "password");
            form.add("client_id", "admin-cli");
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
            Map<?, ?> response = restTemplate.postForObject(url, entity, Map.class);
            
            if (response != null && response.containsKey("access_token")) {
                return String.valueOf(response.get("access_token"));
            }
        } catch (Exception e) {
            log.error("관리자 토큰 획득 실패", e);
        }
        return null;
    }
    
    private String getClientUuid(String adminToken, String clientId) {
        try {
            String url = props.getBaseUrl() + "/admin/realms/" + props.getRealm() + "/clients?clientId=" + clientId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, 
                new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            
            List<Map<String, Object>> clients = response.getBody();
            if (clients != null && !clients.isEmpty()) {
                return String.valueOf(clients.get(0).get("id"));
            }
        } catch (Exception e) {
            log.error("클라이언트 '{}' 조회 실패", clientId, e);
        }
        return null;
    }
    
    private String getServiceAccountUserId(String adminToken, String clientUuid) {
        try {
            String url = props.getBaseUrl() + "/admin/realms/" + props.getRealm() + "/clients/" + clientUuid + "/service-account-user";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, 
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> user = response.getBody();
            if (user != null && user.containsKey("id")) {
                return String.valueOf(user.get("id"));
            }
        } catch (Exception e) {
            log.error("Service Account 사용자 조회 실패", e);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getCurrentRoles(String adminToken, String userId, String clientUuid) {
        try {
            String url = props.getBaseUrl() + "/admin/realms/" + props.getRealm() + 
                        "/users/" + userId + "/role-mappings/clients/" + clientUuid;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, 
                new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            
            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            log.debug("현재 역할 조회 실패 (빈 목록 반환)", e);
            return new ArrayList<>();
        }
    }
    
    private boolean isRoleAssigned(List<Map<String, Object>> currentRoles, String roleName) {
        return currentRoles.stream()
            .anyMatch(role -> roleName.equals(role.get("name")));
    }
    
    private boolean assignRole(String adminToken, String userId, String clientUuid, String roleName) {
        try {
            // 먼저 역할 정보 조회
            String roleUrl = props.getBaseUrl() + "/admin/realms/" + props.getRealm() + 
                           "/clients/" + clientUuid + "/roles/" + roleName;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            HttpEntity<Void> roleEntity = new HttpEntity<>(headers);
            
            ResponseEntity<Map<String, Object>> roleResponse = restTemplate.exchange(
                roleUrl, HttpMethod.GET, roleEntity, 
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> role = roleResponse.getBody();
            if (role == null) {
                log.warn("   ⚠️  역할 '{}'이 존재하지 않습니다.", roleName);
                return false;
            }
            
            // 역할 할당
            String assignUrl = props.getBaseUrl() + "/admin/realms/" + props.getRealm() + 
                             "/users/" + userId + "/role-mappings/clients/" + clientUuid;
            
            headers.setContentType(MediaType.APPLICATION_JSON);
            List<Map<String, Object>> rolesToAssign = Collections.singletonList(role);
            HttpEntity<List<Map<String, Object>>> assignEntity = new HttpEntity<>(rolesToAssign, headers);
            
            ResponseEntity<Void> response = restTemplate.exchange(
                assignUrl, HttpMethod.POST, assignEntity, Void.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 409) {
                // 이미 할당된 경우
                return true;
            }
            log.error("역할 '{}' 할당 실패: {}", roleName, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("역할 '{}' 할당 중 오류 발생", roleName, e);
            return false;
        }
    }
}

