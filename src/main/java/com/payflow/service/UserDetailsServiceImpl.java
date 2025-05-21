package com.payflow.service;

import com.payflow.dto.TransactionAuditDTO;
import com.payflow.model.Admin;
import com.payflow.model.Transaction;
import com.payflow.model.User;
import com.payflow.repository.AdminRepository;
import com.payflow.repository.TransactionRepository;
import com.payflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {


        private final UserRepository userRepository;
        private final AdminRepository adminRepository;
        private final TransactionRepository transactionRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            // First check if it's a regular user
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
                return new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPasswordHash(),
                        authorities
                );
            }

            // Then check if it's an admin
            Optional<Admin> adminOpt = adminRepository.findByUsername(username);
            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                if (!admin.isActive()) {
                    throw new UsernameNotFoundException("Admin account is inactive");
                }

                List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ADMIN"));
                return new org.springframework.security.core.userdetails.User(
                        admin.getUsername(),
                        admin.getPassword(),
                        authorities
                );
            }

            throw new UsernameNotFoundException("User or Admin not found with username: " + username);
        }

// --- other existing methods below remain unchanged ---

        public boolean updateAccountStatus(String username, boolean isActive) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.isFrozen()) {
                    throw new RuntimeException("User account is frozen. Please contact support.");
                }
                user.setStatus(isActive ? User.Status.ACTIVE : User.Status.INACTIVE);
                userRepository.save(user);
                return true;
            }
            return false;
        }

        public List<Transaction> getAllTransactions() {
            return transactionRepository.findAll();
        }

        public boolean setFrozenStatus(String username, boolean freeze) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setFrozen(freeze);
                userRepository.save(user);
                return true;
            }
            return false;
        }

        public List<TransactionAuditDTO> getAllTransactionAudits() {
            List<Transaction> transactions = transactionRepository.findAll();
            return transactions.stream().map(tx -> {
                TransactionAuditDTO dto = new TransactionAuditDTO();
                dto.setId(tx.getId());
                dto.setSenderUsername(tx.getSender().getUsername());
                dto.setReceiverUsername(tx.getReceiver().getUsername());
                dto.setAmount(tx.getAmount());
                dto.setTransactionDate(tx.getTransactionDate());
                dto.setStatus(tx.getStatus());
                return dto;
            }).collect(Collectors.toList());
        }
    }

