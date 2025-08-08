package com.crm.backend.controllers;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import com.crm.backend.services.JwtService;
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
    JwtService jwtService;

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
    // Validate email
    if (otpRequest.getEmail() == null || otpRequest.getEmail().trim().isEmpty()) {
        throw new IllegalArgumentException("Email is required");
    }

    // Validate role
    if (otpRequest.getRole() == null || otpRequest.getRole().trim().isEmpty()) {
        throw new IllegalArgumentException("Role is required");
    }

    // Valid roles
    List<String> validRoles = Arrays.asList(
        "administrator",
        "Sales Representative", 
        "Sales Manager",
        "Marketing Professional",
        "Customer Support",
        "Executive",
        "finance_officer",
        "support_user"
    );

    if (!validRoles.contains(otpRequest.getRole())) {
        throw new IllegalArgumentException("Invalid role selected");
    }

    String otp = generateOtp(6);
    Long otpValue = Long.parseLong(otp);

    // ✅ Now we handle if the user is not in DB
    boolean success = otpService.saveOtp(otpRequest.getEmail(), otpRequest.getRole(), otpValue);
    if (!success) {
        throw new RuntimeException("User with this email and role does not exist");
    }

    // Now send the OTP email
    try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("regenxscout@regensportz.com");
        message.setTo(otpRequest.getEmail());
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp + "\n\nRole: " + otpRequest.getRole());
        javaMailSender.send(message);
    } catch (Exception e) {
        log.error("Exception while sending OTP email", e);
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
        Long otpValue = Long.parseLong(verificationRequest.getOtp());
        Otp otp = otpService.findByEmailAndOtp(verificationRequest.getEmail(), otpValue);
        
        // ✅ No longer deleting OTP - instead, it's marked as "used" in verifyOtp()
        
        // Get the user's role
        String userRole = verificationRequest.getuserRole();

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(verificationRequest.getEmail(), userRole);
        String refreshToken = jwtService.generateRefreshToken(verificationRequest.getEmail());

        response.put("status", "success");
        response.put("access_token", accessToken);
        response.put("refresh_token", refreshToken);
        response.put("role", userRole);
        response.put("message", "OTP verified successfully");
    } else {
        response.put("status", "error");
        response.put("message", "Invalid OTP or role mismatch");
    }
    return response;
}


}