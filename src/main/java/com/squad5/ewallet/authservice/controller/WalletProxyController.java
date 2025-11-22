package com.squad5.ewallet.authservice.controller;

import com.squad5.ewallet.authservice.config.JwtTokenUtil;   // your existing util
import com.squad5.ewallet.authservice.dto.WalletDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletProxyController {

    private final WebClient webClient;
    private final JwtTokenUtil jwtTokenUtil;
    private final String walletBaseUrl;

    public WalletProxyController(WebClient.Builder webClientBuilder,
                                 JwtTokenUtil jwtTokenUtil,
                                 @Value("${wallet.service.base-url}") String walletBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(walletBaseUrl).build();
        this.jwtTokenUtil = jwtTokenUtil;
        this.walletBaseUrl = walletBaseUrl;
    }

    /**
     * Proxy endpoint: validates token locally and forwards request to wallet service.
     * Example: GET /api/wallet/userdetails?userId=1
     */
    @GetMapping("/userdetails")
    public ResponseEntity<?> getUserWallet(@RequestParam("userId") Long userId,
                                           @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        // 1. Validate presence of header
        System.out.println(">>> Incoming Authorization header: " + authorizationHeader);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String t = authorizationHeader.substring(7).trim();
            System.out.println(">>> token length: " + t.length());
            System.out.println(">>> token parts: " + t.split("\\.").length);
        }
        String token = authorizationHeader.substring(7);

        // 2. Parse/validate JWT locally
        try {
            var claimsJws = jwtTokenUtil.parseToken(token);
            var claims = claimsJws.getBody();

            // get subject (sub) and compare with requested userId
            String sub = claims.getSubject();
            if (sub == null) return unauthorized();

            Long tokenUserId;
            try {
                tokenUserId = Long.parseLong(sub);
            } catch (NumberFormatException e) {
                // if your tokens store different claim name, try alternative claims:
                Object uid = claims.get("userId");
                if (uid != null) {
                    tokenUserId = Long.valueOf(String.valueOf(uid));
                } else {
                    return unauthorized();
                }
            }

            if (!tokenUserId.equals(userId)) {
                return unauthorized();
            }

            // 3. Forward request to wallet service, passing Authorization header
            String forwardUri = "/api/wallet/userdetails?userId=" + userId;
            WalletDto wallet = webClient.get()
                    .uri(forwardUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(org.springframework.http.HttpStatusCode::is4xxClientError, resp -> {
                        int status = resp.statusCode().value();
                        if (status == HttpStatus.NOT_FOUND.value()) {
                            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
                        }
                        // If wallet service returns 401, bubble up as Unauthorized to client
                        if (status == HttpStatus.UNAUTHORIZED.value()) {
                            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please Login"));
                        }
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream 4xx"));
                    })
                    .onStatus(org.springframework.http.HttpStatusCode::is5xxServerError, resp -> Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Wallet service error")))
                    .bodyToMono(WalletDto.class)
                    .block();

            return ResponseEntity.ok(wallet);

        } catch (io.jsonwebtoken.JwtException ex) {
            // invalid token
            return unauthorized();
        } catch (ResponseStatusException rse) {
            // map upstream responses
            int status = rse.getStatusCode().value();
            if (status == HttpStatus.UNAUTHORIZED.value()) return unauthorized();
            if (status == HttpStatus.NOT_FOUND.value()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Wallet not found"));
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", "Wallet service error"));
        } catch (Exception e) {
            // generic fallback
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Internal error"));
        }
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please Login"));
    }
}
