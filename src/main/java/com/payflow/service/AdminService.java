package com.payflow.service;

import com.payflow.model.Admin;
import com.payflow.model.AdminRefreshToken;
import com.payflow.repository.AdminRepository;
import com.payflow.repository.AdminRefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminRefreshTokenRepository adminRefreshTokenRepository;  // <-- inject here

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Authenticate admin and save last login timestamp
    public Admin authenticate(String username, String rawPassword) {
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);

        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (passwordEncoder.matches(rawPassword, admin.getPassword()) && admin.isActive()) {
                admin.setLastLogin(LocalDateTime.now());
                adminRepository.save(admin);
                return admin;
            }
        }
        return null;
    }

    // Save refresh token linked to admin after login or token refresh
    public void saveRefreshToken(Admin admin, String refreshToken, LocalDateTime expiryDate) {
        AdminRefreshToken token = new AdminRefreshToken();
        token.setAdmin(admin);                // link admin entity here
        token.setRefreshToken(refreshToken);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiryDate(expiryDate);

        adminRefreshTokenRepository.save(token);
    }

    // Logout method
    public boolean logout(Long adminId) {
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            admin.setLastLogout(LocalDateTime.now());
            adminRepository.save(admin);
            return true;
        }
        return false;
    }
}
