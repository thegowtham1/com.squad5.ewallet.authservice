package com.squad5.ewallet.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable=false)
    private Long merchantUserId; // FK to users.id

    @Column(nullable=false)
    private String name;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(nullable=false)
    private Long priceCents;

    @Column(length=3, nullable=false)
    private String currency = "USD";
}

