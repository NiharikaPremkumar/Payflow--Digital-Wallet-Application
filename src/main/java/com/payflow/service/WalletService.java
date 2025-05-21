package com.payflow.service;

import com.payflow.model.User;
import com.payflow.model.Wallet;
import com.payflow.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    // Method to create a wallet for the user
    public Wallet createWallet(User user) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.valueOf(0.00));
        return walletRepository.save(wallet);
    }

    // Method to get the balance of a user's wallet
    public BigDecimal getBalance(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"))
                .getBalance();
    }

    // Method to add funds to the user's wallet
    public Wallet addFunds(User user, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // Add the specified amount to the wallet balance
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(java.time.Instant.now());  // Update the wallet's 'updatedAt' timestamp

        return walletRepository.save(wallet);  // Save and return the updated wallet
    }

    // New method to get the Wallet entity by User
    public Wallet getWalletByUser(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }
}
