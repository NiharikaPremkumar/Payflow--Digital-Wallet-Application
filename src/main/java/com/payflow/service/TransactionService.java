package com.payflow.service;

import com.payflow.model.Transaction;
import com.payflow.model.User;
import com.payflow.model.Wallet;
import com.payflow.repository.TransactionRepository;
import com.payflow.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public Wallet sendMoney(User sender, User receiver, BigDecimal amount) {

        Wallet senderWallet = walletRepository.findByUser(sender)
                .orElseThrow(() -> new RuntimeException("Sender's wallet not found"));

        Wallet receiverWallet = walletRepository.findByUser(receiver)
                .orElseThrow(() -> new RuntimeException("Receiver's wallet not found"));

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        // Deduct from sender and add to receiver
        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

        // Save both wallets
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);


        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(amount);
        transaction.setStatus("SUCCESS");  // Set the status as needed

        transactionRepository.save(transaction); // Save the transaction to DB

        // After saving the transaction, return the updated sender's wallet
        return senderWallet;
    }

    public List<Transaction> getTransactionHistory(User user) {
        return transactionRepository.findBySenderOrReceiver(user, user);
    }
}


