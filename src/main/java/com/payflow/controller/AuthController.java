package com.payflow.controller;

import com.payflow.dto.*;
import com.payflow.model.RefreshToken;
import com.payflow.model.User;
import com.payflow.model.Wallet;
import com.payflow.repository.RefreshTokenRepository;
import com.payflow.repository.UserRepository;
import com.payflow.repository.WalletRepository;
import com.payflow.service.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
     @Autowired
    private final RefreshTokenRepository refreshTokenRepository;


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest req) {
        logger.info("Signup attempt for username: {}", req.getUsername());

        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is already taken"));
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is already registered"));
        }

        // Create and save user
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setFullName(req.getFullName());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user = userRepository.save(user);

        // Create wallet
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency("INR");
        walletRepository.save(wallet);

        // Generate access token
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPasswordHash(), new ArrayList<>()
        );
        String accessToken = jwtUtil.generateToken(userDetails);

        // Generate and save refresh token
        String refreshTokenStr = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User Registered Successfully");
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshTokenStr);

        logger.info("User registered successfully: {}", req.getUsername());
        return ResponseEntity.ok(response);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        logger.info("Login attempt for username: {}", req.getUsername());
        try {
            Optional<User> userOpt = userRepository.findByUsername(req.getUsername());
            if (userOpt.isEmpty()) {
                logger.warn("Login failed: user not found for username={}", req.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid credentials"));
            }

            User user = userOpt.get();
            if (user.isFrozen()) {
                logger.warn("Login failed: account frozen for username={}", req.getUsername());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Account is frozen.Please contact admin for support"));
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken((UserDetails) authentication.getPrincipal());

            if (walletRepository.findByUser(user).isEmpty()) {
                Wallet newWallet = new Wallet();
                newWallet.setUser(user);
                newWallet.setBalance(BigDecimal.ZERO);
                newWallet.setCurrency("INR");
                walletRepository.save(newWallet);
            }

            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setUser(user);
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
            refreshTokenRepository.save(refreshToken);

            logger.info("Login successful for username: {}", req.getUsername());
            return ResponseEntity.ok(new LoginResponse(jwt, refreshToken.getToken()));
        } catch (Exception e) {
            logger.error("Login failed for username: {} - {}", req.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        logger.info("Logout request received for refresh token: {}", request.getRefreshToken());

        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired refresh token"));
        User user = token.getUser();

        user.setLastLogout(Instant.now());
        userRepository.save(user);

        refreshTokenRepository.delete(token);

        logger.info("Logout successful for token: {}", request.getRefreshToken());

        LogoutResponse lr = new LogoutResponse("Logged out successfully", "");
        return ResponseEntity.ok(lr);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            logger.warn("User info requested but no user authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("User not authenticated"));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        logger.info("User info requested for username: {}", user.getUsername());

        UserResponse userResponse = new UserResponse(user.getFullName(), user.getUsername(), user.getEmail());
        return ResponseEntity.ok(userResponse);
    }
}
