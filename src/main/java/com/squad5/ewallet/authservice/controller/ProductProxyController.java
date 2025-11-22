package com.squad5.ewallet.authservice.controller;

import com.squad5.ewallet.authservice.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductProxyController {

    private final WebClient webClient;
    private final JwtTokenUtil jwtTokenUtil;

    public ProductProxyController(WebClient.Builder webClientBuilder,
                                  JwtTokenUtil jwtTokenUtil,
                                  @Value("${product.service.base-url}") String productBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(productBaseUrl).build();
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllProducts(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        // 1. Validate presence of header
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return unauthorized();
        }
        String token = authorizationHeader.substring(7);

        // 2. Parse/validate JWT locally
        try {
            var claimsJws = jwtTokenUtil.parseToken(token);
            var claims = claimsJws.getBody();

            // Ensure token is valid (has subject)
            String sub = claims.getSubject();
            if (sub == null) return unauthorized();

            // 3. Forward request to product service
            String forwardUri = "/api/products/all";
            List<?> products = webClient.get()
                    .uri(forwardUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(org.springframework.http.HttpStatusCode::is4xxClientError, resp -> {
                        int status = resp.statusCode().value();
                        if (status == HttpStatus.NOT_FOUND.value()) {
                            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Products not found"));
                        }
                        if (status == HttpStatus.UNAUTHORIZED.value()) {
                            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please Login"));
                        }
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream 4xx"));
                    })
                    .onStatus(org.springframework.http.HttpStatusCode::is5xxServerError,
                              resp -> Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service error")))
                    .bodyToFlux(Object.class)
                    .collectList()
                    .block();

            return ResponseEntity.ok(products);

        } catch (io.jsonwebtoken.JwtException ex) {
            // Invalid token
            return unauthorized();
        } catch (ResponseStatusException rse) {
            // Map upstream responses
            int status = rse.getStatusCode().value();
            if (status == HttpStatus.UNAUTHORIZED.value()) return unauthorized();
            if (status == HttpStatus.NOT_FOUND.value())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Products not found"));
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", "Product service error"));
        } catch (Exception e) {
            // Generic fallback
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Internal error"));
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(
            @PathVariable Long productId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        // 1. Validate presence of header
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return unauthorized();
        }
        String token = authorizationHeader.substring(7);

        // 2. Parse/validate JWT locally
        try {
            var claimsJws = jwtTokenUtil.parseToken(token);
            var claims = claimsJws.getBody();

            String sub = claims.getSubject();
            if (sub == null) return unauthorized();

            // 3. Forward request to product service
            String forwardUri = "/api/products/" + productId;
            Object product = webClient.get()
                    .uri(forwardUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(org.springframework.http.HttpStatusCode::is4xxClientError, resp -> {
                        int status = resp.statusCode().value();
                        if (status == HttpStatus.NOT_FOUND.value()) {
                            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
                        }
                        if (status == HttpStatus.UNAUTHORIZED.value()) {
                            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please Login"));
                        }
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream 4xx"));
                    })
                    .onStatus(org.springframework.http.HttpStatusCode::is5xxServerError,
                              resp -> Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service error")))
                    .bodyToMono(Object.class)
                    .block();

            return ResponseEntity.ok(product);

        } catch (io.jsonwebtoken.JwtException ex) {
            return unauthorized();
        } catch (ResponseStatusException rse) {
            int status = rse.getStatusCode().value();
            if (status == HttpStatus.UNAUTHORIZED.value()) return unauthorized();
            if (status == HttpStatus.NOT_FOUND.value())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Product not found"));
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", "Product service error"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Internal error"));
        }
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please Login"));
    }
}

