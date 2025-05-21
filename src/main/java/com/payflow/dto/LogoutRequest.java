package com.payflow.dto;

import lombok.Data;
@Data
public class LogoutRequest {
    private String refreshToken;
    private String username;

    public String getRefreshToken() {
        return refreshToken;
    }


    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

