package com.squad5.ewallet.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class AuthResponse {
    private String userId;
    private String accessToken;
}

