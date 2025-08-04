package com.crm.backend.controllers;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crm.backend.dto.OtpRequest;
import com.crm.backend.dto.OtpVerificationRequest;
import com.crm.backend.entity.Otp;
import com.crm.backend.repository.UserRepo;
import com.crm.backend.services.OtpService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth/otp")
@Slf4j
public class OtpGen {

    private static final SecureRandom random = new SecureRandom();
    
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private OtpService otpService;

    private String generateOtp(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    @PostMapping("generate")
    public Map<String, String> generateOtpAndSend(@RequestBody OtpRequest otpRequest) {
        String otp = generateOtp(6);
        Long otpValue = Long.parseLong(otp); // Convert String OTP to Long

        // Get user role (you need to implement this based on your User entity)
        String userRole = "USER"; // Default role or get from userRepo
        
        // store otp
        otpService.saveOtp(otpRequest.getEmail(), userRole, otpValue);

        // send otp to email
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("regenxscout@regensportz.com");
            message.setTo(otpRequest.getEmail());
            message.setSubject("Your OTP Code");
            message.setText("Your OTP Is: " + otp);
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("Exception while sending the OTP to mail", e);
            throw new RuntimeException("Failed to send OTP email");
        }

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "OTP sent to email: " + otpRequest.getEmail());
        return response;
    }

    // verify otp

    @PostMapping("verify")
    public Map<String, Object> verifyOtp(@RequestBody OtpVerificationRequest verificationRequest) {
        boolean isValid = otpService.verifyOtp(
            verificationRequest.getEmail(),
            verificationRequest.getOtp(),
            verificationRequest.getuserRole()
        );
        
        Map<String, Object> response = new HashMap<>();
        if (isValid) {
            response.put("status", "success");
            response.put("message", "OTP verified successfully");
            
            // Optionally mark OTP as used
            Long otpValue = Long.parseLong(verificationRequest.getOtp());
            Otp otp = otpService.findByEmailAndOtp(verificationRequest.getEmail(), otpValue);
            otpService.deleteOtp(otp);
        } else {
            response.put("status", "error");
            response.put("message", "Invalid OTP or role mismatch");
        }
        
        return response;
    }
}