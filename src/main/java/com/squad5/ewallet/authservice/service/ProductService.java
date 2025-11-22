package com.squad5.ewallet.authservice.service;

import com.squad5.ewallet.authservice.entity.Product;
import com.squad5.ewallet.authservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired private ProductRepository productRepository;

    public List<Product> listAll() {
        return productRepository.findAll();
    }

    public Optional<Product> getById(Long id) {
        return productRepository.findById(id);
    }
}

