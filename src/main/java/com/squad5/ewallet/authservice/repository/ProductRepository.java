package com.squad5.ewallet.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.squad5.ewallet.authservice.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}

