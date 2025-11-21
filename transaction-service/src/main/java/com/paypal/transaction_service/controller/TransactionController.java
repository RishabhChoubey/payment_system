package com.paypal.transaction_service.controller;


import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.service.TransactionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.paypal.transaction_service.dto.TransactionRequestDTO;
import java.time.LocalDateTime;
import com.paypal.transaction_service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import com.paypal.transaction_service.client.UserServiceClient;

@RestController
@RequestMapping("/api/transactions/")
public class TransactionController {
    private final TransactionService service;
    private final UserServiceClient userServiceClient;
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    public TransactionController(TransactionService service, UserServiceClient userServiceClient) {
        this.service = service;
        this.userServiceClient = userServiceClient;
    }
    @Value("${jwt.secret:secret}")
    private String jwtSecret;

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody TransactionRequestDTO requestDto, @RequestHeader("Authorization") String authHeader) {
        // Get senderId from user-service API
        Long senderId = userServiceClient.getCurrentUserId(authHeader);

        // Lookup receiverId by receiverEmail from user-service
        Long receiverId = userServiceClient.getUserIdByEmail(requestDto.getReceiverEmail(), authHeader);

        Transaction transaction = new Transaction();
        transaction.setSenderId(senderId);
        transaction.setReceiverId(receiverId);
        transaction.setAmount(requestDto.getAmount());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("PENDING");
        // Optionally set note if your entity supports it

        Transaction created = service.createTransaction(transaction);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/all")
    public List<Transaction> getAll(HttpServletRequest request) {
        // Debug logging to inspect incoming Authorization header and security principal
        String authHeader = request.getHeader("Authorization");
        logger.debug("/api/transactions/all called. Authorization header present: {}", authHeader != null ? "yes" : "no");
        if (authHeader != null) {
            logger.debug("Authorization header (first 64 chars): {}", authHeader.length() > 64 ? authHeader.substring(0,64) + "..." : authHeader);
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("SecurityContext authentication present: {}", auth != null && auth.isAuthenticated());
        if (auth != null && auth.isAuthenticated()) {
            logger.debug("SecurityContext principal='{}'", auth.getName());
        }

        return service.getAllTransactions();
    }

}
