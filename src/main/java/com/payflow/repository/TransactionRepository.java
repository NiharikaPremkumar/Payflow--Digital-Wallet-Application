package com.payflow.repository;

import com.payflow.model.Transaction;
import com.payflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderOrReceiver(User sender, User receiver);;
}

