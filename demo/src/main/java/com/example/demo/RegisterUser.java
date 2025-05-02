package com.example.demo;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public class RegisterUser {
    private static final int SALT_LENGTH = 32;
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    public static void registerUser(String username, String password) throws SQLException {
        String salt = generateSalt();
        String passwordHash = hashPassword(password, salt);
        UserRepository.createUser(username, passwordHash, salt);
    }

    public static void updatePassword(String username, String newPassword) throws SQLException {
        String salt = generateSalt();
        String passwordHash = hashPassword(newPassword, salt);
        UserRepository.updatePassword(username, passwordHash, salt);
    }

    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

        public static String hashPassword(String password, String salt) {
            try {
                byte[] saltBytes = Base64.getDecoder().decode(salt);
                PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    saltBytes,
                    ITERATIONS,
                    KEY_LENGTH
                );
                SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                byte[] hash = skf.generateSecret(spec).getEncoded();
                return Base64.getEncoder().encodeToString(hash);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
