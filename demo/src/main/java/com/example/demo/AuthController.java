package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

// Use Jakarta EE imports for newer Spring versions
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;  // Add this import
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Map;

@Controller
public class AuthController {
    private static final int PASSWORD_STRENGTH = 8;
    private static final int SALT_LENGTH = 32;
    private static final int HASH_ITERATIONS = 10000;
    private static final int HASH_KEY_LENGTH = 256;

  
    @PostMapping("/login")
public ResponseEntity<?> loginUser(
        @RequestParam String username,
        @RequestParam String password) {
    
    try {
        // 1. Get user from database
        String[] hashAndSalt = UserRepository.getPasswordHashAndSalt(username);
        if (hashAndSalt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password"));
        }

        String storedHash = hashAndSalt[0];
        String salt = hashAndSalt[1];
        
        // 2. Hash the provided password with the stored salt
        String computedHash = RegisterUser.hashPassword(password, salt);
        
        // 3. Compare hashes
        if (!computedHash.equals(storedHash)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password"));
        }
        
        // 4. Login successful
        return ResponseEntity.ok(Map.of("message", "Login successful"));
        
    } catch (Exception e) {
        return ResponseEntity.internalServerError()
            .body(Map.of("error", "Server error: " + e.getMessage()));
    }
 

}
    @PostMapping("/register")
    public void registerUser(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response) throws IOException {
        
        // Validate input
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Username and password are required");
            return;
        }

        if (password.length() < PASSWORD_STRENGTH) {
            response.sendRedirect("/travelpage/register.html?error=Password must be at least " + 
                                 PASSWORD_STRENGTH + " characters");
            return;
        }

        try {
            // Check if username exists
            if (UserRepository.usernameExists(username)) {
                response.sendRedirect("/travelpage/register.html?error=Username already exists");
                return;
            }

            // Register new user
            RegisterUser.registerUser(username, password);
            response.sendRedirect("/travelpage/login.html?success=Registration successful");
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    // Forgot password endpoint
    @PostMapping("/forgot-password")
    public void forgotPassword(
            @RequestParam String username,
            HttpServletResponse response) throws IOException {
        
        try {
            if (!UserRepository.usernameExists(username)) {
                response.sendRedirect("/travelpage/forgot-password.html?error=Username not found");
                return;
            }

            // Generate and store reset token
            String resetToken = generateSecureToken();
            UserRepository.storeResetToken(username, resetToken);

            // Send password reset email (in real app)
            sendPasswordResetEmail(username, resetToken);

            response.sendRedirect("/travelpage/login.html?info=Password reset instructions sent to your email");
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    // Password reset endpoint
    @PostMapping("/reset-password")
    public void resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            HttpServletResponse response) throws IOException {
        
        try {
            // Validate token and get username
            String username = UserRepository.validateResetToken(token);
            if (username == null) {
                response.sendRedirect("/travelpage/reset-password.html?error=Invalid or expired token");
                return;
            }

            // Update password
            RegisterUser.updatePassword(username, newPassword);
            
            // Invalidate token
            UserRepository.invalidateResetToken(token);

            response.sendRedirect("/travelpage/login.html?success=Password updated successfully");
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }
    
    // Helper methods
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void sendPasswordResetEmail(String username, String token) {
        // In a real application, implement email sending logic here
        String resetLink = "https://yourapp.com/reset-password?token=" + token;
        System.out.println("Password reset link for " + username + ": " + resetLink);
    }
}