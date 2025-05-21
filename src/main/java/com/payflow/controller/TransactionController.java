package com.payflow.controller;

import com.payflow.dto.SendMoneyRequest;
import com.payflow.model.Transaction;
import com.payflow.model.User;
import com.payflow.model.Wallet;
import com.payflow.service.TransactionService;
import com.payflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    public static class ResponseObject {
        private boolean success;
        private String message;
        private Object data;

        public ResponseObject(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }

    @PostMapping("/send-money")
    public ResponseEntity<?> sendMoney(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestBody SendMoneyRequest request) {
        logger.info("Send money request from user: {} to user: {} amount: {}",
                userDetails != null ? userDetails.getUsername() : "null",
                request.getReceiverUsername(), request.getAmount());

        if (userDetails == null) {
            logger.warn("Unauthorized send money attempt");
            return ResponseEntity.status(401).body(new ResponseObject(false, "Unauthorized", null));
        }

        User sender = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Optional<User> receiverOpt = userRepository.findByUsername(request.getReceiverUsername());
        if (receiverOpt.isEmpty()) {
            logger.warn("Receiver not found: {}", request.getReceiverUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(false, "Receiver not found", null));
        }
        User receiver = receiverOpt.get();


        if (sender.getUsername().equals(receiver.getUsername())) {
            logger.warn("Send money failed: User {} attempted to send money to self", sender.getUsername());
            return ResponseEntity.badRequest().body(new ResponseObject(false, "Cannot send money to yourself", null));
        }

        Wallet senderWallet = transactionService.sendMoney(sender, receiver, request.getAmount());

        if (senderWallet != null) {
            logger.info("Money sent successfully from {} to {} amount {}", sender.getUsername(), receiver.getUsername(), request.getAmount());
            return ResponseEntity.ok(new ResponseObject(true, "Money sent successfully", senderWallet));
        } else {
            logger.error("Failed to send money from {} to {}", sender.getUsername(), receiver.getUsername());
            return ResponseEntity.status(400).body(new ResponseObject(false, "Failed to send money", null));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getTransactionHistory(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Transaction history requested by user: {}", userDetails != null ? userDetails.getUsername() : "null");

        if (userDetails == null) {
            logger.warn("Unauthorized transaction history request");
            return ResponseEntity.status(401).body("Unauthorized");
        }

        User sender = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        List<Transaction> transactions = transactionService.getTransactionHistory(sender);

        logger.info("Transaction history returned for user: {}", userDetails.getUsername());
        return ResponseEntity.ok(transactions);
    }
}
