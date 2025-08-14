package com.job.service.impl;

import com.job.dao.entity.OtpDetails;
import com.job.dao.repository.OtpDetailsRepository;
import com.job.exception.custom.NotFoundException;
import com.job.model.dto.request.RegistrationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.job.exception.constant.ErrorCode.NOT_FOUND;
import static com.job.exception.constant.ErrorMessage.NOT_FOUND_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpDetailsRepository otpDetailsRepository;
    private final PasswordEncoder passwordEncoder;

    public String generateOtp(RegistrationRequest request) {
        log.info("Generating otp code for verify account, email {}", request.getEmail());
        String code = generateOtpCode();

        OtpDetails otpDetails = OtpDetails.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .hashedPassword(passwordEncoder.encode(request.getPassword()))
                .code(code)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        otpDetailsRepository.save(otpDetails);
        return code;
    }

    public void generateOtp(String email) {
        log.info("Generating otp code for reset password, email {}", email);
        String code = generateOtpCode();

        OtpDetails otpDetails = OtpDetails.builder()
                .email(email)
                .code(code)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        otpDetailsRepository.save(otpDetails);
    }

    public boolean verifyOtp(String email, String otp) {
        log.info("Verifying otp code for email {}", email);
        OtpDetails otpDetails = otpDetailsRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_MESSAGE, "Otp details"),
                        NOT_FOUND));

        if (!otpDetails.getCode().equals(otp)) {
            return false;
        }

        return !otpDetails.getExpiryDate().isBefore(LocalDateTime.now());
    }

    public void deleteOtp(String email) {
        log.info("Deleting otp for email {}", email);
        OtpDetails otpDetails = otpDetailsRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_MESSAGE, "Otp"),
                        NOT_FOUND));

        otpDetailsRepository.delete(otpDetails);
    }

    public void deleteAllOtpByEmail(String email) {
        log.info("Deleting all otp by email {}", email);

        otpDetailsRepository.deleteAllByEmail(email);
    }

    public OtpDetails findByEmail(String email) {
        return otpDetailsRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_MESSAGE, "Otp details"),
                        NOT_FOUND));
    }

    public OtpDetails findLatestOtpDetails(String email) {
        return otpDetailsRepository.findTopByEmailOrderByExpiryDateDesc(email)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_MESSAGE, "Otp code"), NOT_FOUND));
    }

    public String findLatestOtpCode(String email) {
        OtpDetails otpDetails = otpDetailsRepository.findTopByEmailOrderByExpiryDateDesc(email)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_MESSAGE, "Otp code"), NOT_FOUND));

        return otpDetails.getCode();
    }

    private String generateOtpCode() {
        return String.valueOf(100000 + Math.round(Math.random() * 900000));//100000-1000000
    }

}
