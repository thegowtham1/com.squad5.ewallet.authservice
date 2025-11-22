package com.squad5.ewallet.authservice.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String mobileNumber;
    private String password;
    private String displayName;
    //default CUSTOMER
    private String role;
}

