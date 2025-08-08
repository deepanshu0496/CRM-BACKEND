package com.crm.backend.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crm.backend.entity.Otp;
import com.crm.backend.entity.User;
import com.crm.backend.repository.OtpRepo;
import com.crm.backend.repository.UserRepo;

/**
 * Service class for handling OTP (One-Time Password) operations including:
 * - Generation, validation, and management of OTPs
 * - Email delivery of OTPs
 * - Scheduled cleanup of expired OTPs
 */
@Service
public class OtpService {
    
    private static final int OTP_VALIDITY_MINUTES = 5;
    private static final int OTP_CLEANUP_MINUTES = 30;
    private static final int OTP_LENGTH = 6;

    @Autowired
    private OtpRepo otpRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * Sends OTP to user email if the user exists in the system.
     *
     * @param userEmail Email address of the user
     * @param userRole Role of the user
     * @throws RuntimeException if user not found
     */
    public void sendOtpIfUserExists(String userEmail, String userRole) {
        Optional<User> userOpt = userRepo.findByUserEmailAndUserRole(userEmail, userRole);

        if (userOpt.isPresent()) {
            Long otpValue = generateOtp();
            saveNewOtp(userEmail, userRole, otpValue);
            sendOtpEmail(userEmail, otpValue);
        } else {
            throw new RuntimeException(
                String.format("User not found for email: %s and role: %s", userEmail, userRole)
            );
        }
    }

    /**
     * Saves a new OTP record in the database.
     *
     * @param userEmail Email address associated with the OTP
     * @param userRole User role associated with the OTP
     * @param otpValue The OTP value to be saved
     * @return true if saved successfully, false if user doesn't exist
     */
    @Transactional
    public boolean saveOtp(String userEmail, String userRole, Long otpValue) {
        if (!userRepo.findByUserEmailAndUserRole(userEmail, userRole).isPresent()) {
            return false;
        }

        saveNewOtp(userEmail, userRole, otpValue);
        return true;
    }

    /**
     * Verifies if the provided OTP is valid.
     *
     * @param email Email address to verify
     * @param otp OTP value to verify
     * @param userRole Expected user role (can be null to skip role check)
     * @return true if OTP is valid and not expired, false otherwise
     */
    @Transactional
    public boolean verifyOtp(String email, String otp, String userRole) {
        try {
            Long otpValue = Long.parseLong(otp);
            Otp storedOtp = findByEmailAndOtp(email, otpValue);
            
            if (storedOtp == null || storedOtp.isUsed()) {
                return false;
            }

            boolean isValid = isOtpValid(storedOtp, userRole);

            if (isValid) {
                markOtpAsUsed(storedOtp);
            }
            
            return isValid;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Finds OTP record by email and OTP value.
     *
     * @param email Email address to search for
     * @param otpValue OTP value to search for
     * @return Otp entity if found, null otherwise
     */
    public Otp findByEmailAndOtp(String email, Long otpValue) {
        return otpRepo.findByUserEmailAndOtp(email, otpValue);
    }

    /**
     * Checks if OTP is valid (unused and not expired).
     *
     * @param email Email address associated with the OTP
     * @param otpValue OTP value to check
     * @return true if OTP is valid, false otherwise
     */
    public boolean isOtpValid(String email, Long otpValue) {
        Otp otp = findByEmailAndOtp(email, otpValue);
        return otp != null && isOtpValid(otp, null);
    }

    /**
     * Scheduled job to clean up expired OTPs from database.
     * Runs daily by default.
     */
    @Transactional
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    public void cleanupExpiredOtps() {
        otpRepo.deleteByCreatedAtBefore(
            LocalDateTime.now().minusMinutes(OTP_CLEANUP_MINUTES)
        );
    }

    // ============ PRIVATE HELPER METHODS ============ //

    /**
     * Generates a random 6-digit OTP.
     *
     * @return Generated OTP value
     */
    private Long generateOtp() {
        return 100000L + (long)(Math.random() * 900000);
    }

    /**
     * Sends OTP via email.
     *
     * @param toEmail Recipient email address
     * @param otpValue OTP value to send
     */
    private void sendOtpEmail(String toEmail, Long otpValue) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("regenxscout@regensportz.com");
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");
        message.setText(String.format("Your OTP is: %d", otpValue));
        javaMailSender.send(message);
    }

    /**
     * Creates and saves a new OTP record.
     *
     * @param email Email address to associate with OTP
     * @param role User role to associate with OTP
     * @param otpValue OTP value to save
     */
    private void saveNewOtp(String email, String role, Long otpValue) {
        Otp otp = new Otp();
        otp.setUserEmail(email);
        otp.setUserRole(role);
        otp.setOtp(otpValue);
        otp.setCreatedAt(LocalDateTime.now());
        otp.setUsed(false);
        otpRepo.save(otp);
    }

    /**
     * Marks OTP as used in the database.
     *
     * @param otp Otp entity to mark as used
     */
    private void markOtpAsUsed(Otp otp) {
        otp.setUsed(true);
        otpRepo.save(otp);
    }

    /**
     * Checks if OTP is valid based on creation time and role.
     *
     * @param otp Otp entity to validate
     * @param roleToCheck Role to validate against (can be null)
     * @return true if OTP is valid, false otherwise
     */
    private boolean isOtpValid(Otp otp, String roleToCheck) {
        return otp.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(OTP_VALIDITY_MINUTES))
            && (roleToCheck == null || roleToCheck.equals(otp.getUserRole()));
    }
}