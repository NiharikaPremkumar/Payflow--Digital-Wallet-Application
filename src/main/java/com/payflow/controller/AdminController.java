package com.payflow.controller;

import com.payflow.dto.AdminResponse;
import com.payflow.dto.SignupRequest;
import com.payflow.dto.TransactionAuditDTO;
import com.payflow.model.Admin;
import com.payflow.model.AdminRefreshToken;
import com.payflow.model.User;
import com.payflow.repository.AdminRefreshTokenRepository;
import com.payflow.repository.AdminRepository;
import com.payflow.repository.UserRepository;
import com.payflow.service.AdminService;
import com.payflow.service.JwtUtil;
import com.payflow.service.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userService;
    private final AdminService adminService;
    private final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AdminRefreshTokenRepository adminRefreshTokenRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> registerAdmin(@RequestBody SignupRequest request) {
        if (adminRepository.findByEmail(request.getEmail()).isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Admin already exists with this email");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

        }

        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        adminRepository.save(admin);

        String accessToken = jwtUtil.admingenerateToken(admin.getUsername(), admin.getRole());
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                admin.getUsername(),
                admin.getPassword(),
                new ArrayList<>()
        );
        String refreshToken = jwtUtil.generateToken(userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin registered successfully");
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("username", admin.getUsername());
        response.put("role", admin.getRole());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        logger.info("Admin login attempt: username={}", username);

        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        if (adminOpt.isEmpty() || !passwordEncoder.matches(request.get("password"), adminOpt.get().getPassword())) {
            logger.warn("Admin login failed: invalid credentials for username={}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        Admin admin = adminOpt.get();
        admin.setLastLogin(LocalDateTime.now());
        adminRepository.save(admin);

        // Create UserDetails for token generation
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                admin.getUsername(),
                admin.getPassword(),
                new ArrayList<>()  // Add authorities if you have any
        );

        // Generate tokens
        String accessToken = jwtUtil.admingenerateToken(admin.getUsername(), admin.getRole());
        String refreshToken = jwtUtil.generateToken(userDetails);

        // Save or update refresh token in DB
        Optional<AdminRefreshToken> existingTokenOpt = adminRefreshTokenRepository.findByAdmin(admin);
        AdminRefreshToken refreshTokenEntity;
        if (existingTokenOpt.isPresent()) {
            refreshTokenEntity = existingTokenOpt.get();
            refreshTokenEntity.setRefreshToken(refreshToken);
            refreshTokenEntity.setCreatedAt(LocalDateTime.now()); // update created time or add expiry if needed
        } else {
            refreshTokenEntity = new AdminRefreshToken();
            refreshTokenEntity.setAdmin(admin);
            refreshTokenEntity.setRefreshToken(refreshToken);
            refreshTokenEntity.setCreatedAt(LocalDateTime.now());
        }
        adminRefreshTokenRepository.save(refreshTokenEntity);

        logger.info("Admin login successful: username={}", username);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("accessToken", accessToken);
        responseBody.put("refreshToken", refreshToken);
        responseBody.put("username", admin.getUsername());
        responseBody.put("role", admin.getRole());

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> adminLogout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Logout failed: Missing or invalid Authorization header");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        String username;
        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            logger.error("Logout failed: Invalid JWT token - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token");
        }

        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();


            admin.setLastLogout(LocalDateTime.now());
            adminRepository.save(admin);

            logger.info("Logout successful for username: {}", username);
            return ResponseEntity.ok("Logged out successfully");
        } else {
            logger.warn("Logout failed: Admin user not found for username: {}", username);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token");
        }
    }


    @PostMapping("/freeze-account/{username}")
    public ResponseEntity<?> freezeAccount(@PathVariable String username) {
        logger.info("Freeze account request: username={}", username);

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            logger.warn("Freeze account failed: user not found username={}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AdminResponse(false, "User not found", null));
        }

        User user = userOpt.get();
        if (user.isFrozen()) {
            logger.warn("Freeze skipped: account already frozen username={}", username);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AdminResponse(false, "Account is already frozen", null));
        }

        boolean result = userService.setFrozenStatus(username, true);
        if (result) {
            logger.info("Account frozen successfully: username={}", username);
            return ResponseEntity.ok(new AdminResponse(true, "Account frozen successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AdminResponse(false, "Failed to freeze account", null));
        }
    }

    @PostMapping("/unfreeze-account/{username}")
    public ResponseEntity<?> unfreezeAccount(@PathVariable String username) {
        logger.info("Unfreeze account request: username={}", username);

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            logger.warn("Unfreeze account failed: user not found username={}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AdminResponse(false, "User not found", null));
        }

        User user = userOpt.get();
        if (!user.isFrozen()) {
            logger.warn("Unfreeze skipped: account already active username={}", username);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AdminResponse(false, "Account is already active", null));
        }

        boolean result = userService.setFrozenStatus(username, false);
        if (result) {
            logger.info("Account unfrozen successfully: username={}", username);
            return ResponseEntity.ok(new AdminResponse(true, "Account unfrozen successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AdminResponse(false, "Failed to unfreeze account", null));
        }
    }


    @GetMapping("/audit-transactions")
    public ResponseEntity<?> auditTransactions() {
        logger.info("Transaction audit requested");
        List<TransactionAuditDTO> auditList = userService.getAllTransactionAudits();
        logger.info("Transaction audit data fetched: count={}", auditList.size());
        return ResponseEntity.ok(new AdminResponse(true, "Audit data fetched", auditList));
    }


    @PostMapping("/system-logs")
    public ResponseEntity<?> getSystemLogs() {
        String logFilePath = "logs/payflow-app.log"; // adjust path as needed
        List<Map<String, String>> logs = new ArrayList<>();

        // Regex to match your log line format:
        // 2025-05-19 15:30:01 INFO  com.example.ClassName - Message text here
        String logLineRegex = "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\s+(\\w+)\\s+.*? - (.*)";
        Pattern pattern = Pattern.compile(logLineRegex);

        try {
            List<String> logLines = Files.readAllLines(Paths.get(logFilePath), StandardCharsets.UTF_8);

            for (String line : logLines) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    Map<String, String> logEntry = new HashMap<>();
                    logEntry.put("timestamp", matcher.group(1));
                    logEntry.put("level", matcher.group(2));
                    logEntry.put("message", matcher.group(3));
                    logs.add(logEntry);
                }
            }

            return ResponseEntity.ok(new AdminResponse(true, "Logs fetched successfully", logs));
        } catch (IOException e) {
            logger.error("Error reading log file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AdminResponse(false, "Failed to read logs", null));
        }
    }
}
