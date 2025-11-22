package com.squad5.ewallet.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=100)
    private String username;

    @Column(name="display_name")
    private String displayName;

    @Column(length=100)
    private String name;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(name="mobile_number", nullable=false, unique=true)
    private String mobileNumber;

    @Column(name="password_hash", nullable=false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Role role = Role.CUSTOMER;

    @Column(length=50)
    private String type;

    @Column(nullable=false)
    private String status = "ACTIVE";
}
