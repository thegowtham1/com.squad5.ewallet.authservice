package com.squad5.ewallet.authservice.service;

import com.squad5.ewallet.authservice.util.JwtTokenUtil;
import com.squad5.ewallet.authservice.dto.AuthResponse;
import com.squad5.ewallet.authservice.dto.RegisterRequest;
import com.squad5.ewallet.authservice.entity.Role;
import com.squad5.ewallet.authservice.entity.User;
import com.squad5.ewallet.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenUtil jwtTokenUtil;

    public User register(RegisterRequest req) {
        var user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setMobileNumber(req.getMobileNumber());
        user.setDisplayName(req.getDisplayName());
        user.setName(req.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole() != null ? Role.valueOf(req.getRole()) : Role.CUSTOMER);
        return userRepository.save(user);
    }

    public AuthResponse login(String username, String password) {

        var maybe = userRepository.findByUsername(username);
        if (maybe.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }

        var user = maybe.get();

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtTokenUtil.generateToken(user.getId(), user.getRole().name());

        // return both userId and token
        return new AuthResponse(
                String.valueOf(user.getId()),
                token
        );
    }

}

