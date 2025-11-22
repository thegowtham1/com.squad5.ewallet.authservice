package com.squad5.ewallet.authservice.controller;

import com.squad5.ewallet.authservice.dto.AuthResponse;
import com.squad5.ewallet.authservice.dto.LoginRequest;
import com.squad5.ewallet.authservice.dto.RegisterRequest;
import com.squad5.ewallet.authservice.entity.User;
import com.squad5.ewallet.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        User u = authService.register(req);
        return ResponseEntity.ok(u);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        AuthResponse response = authService.login(req.getUsername(), req.getPassword());
        return ResponseEntity.ok(response);
    }

}
