package com.ctrlf.infra.keycloak.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class PasswordTokenRequest {
    @Schema(example = "user1")
    private String username;
    @Schema(example = "11111")
    private String password;
    @Schema(example = "infra-admin")
    private String clientId;
    @Schema(example = "changeme")
    private String clientSecret;
    @Schema(example = "openid profile email")
    private String scope;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}

