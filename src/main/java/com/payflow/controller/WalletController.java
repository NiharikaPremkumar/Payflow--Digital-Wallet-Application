package com.payflow.controller;

import com.payflow.dto.AddFundsRequest;
import com.payflow.model.User;
import com.payflow.model.Wallet;
import com.payflow.repository.UserRepository;
import com.payflow.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    private final WalletService walletService;
    private final UserRepository userRepository;

    public static class AddFundsResponse {
        private boolean success;
        private String message;
        private Wallet wallet;

        public AddFundsResponse(boolean success, String message, Wallet wallet) {
            this.success = success;
            this.message = message;
            this.wallet = wallet;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Wallet getWallet() { return wallet; }
    }

    @PostMapping("/add-funds")
    public ResponseEntity<?> addFunds(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestBody AddFundsRequest request) {
        logger.info("Add funds request by user: {} amount: {}", userDetails.getUsername(), request.getAmount());

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletService.addFunds(user, request.getAmount());

        logger.info("Funds added successfully for user: {} new balance: {}", user.getUsername(), wallet.getBalance());
        return ResponseEntity.ok(new AddFundsResponse(true, "Funds added successfully", wallet));
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Balance requested by user: {}", userDetails.getUsername());

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletService.getWalletByUser(user);

        logger.info("Balance returned for user: {} balance: {}", user.getUsername(), wallet.getBalance());
        return ResponseEntity.ok(wallet);
    }
}

