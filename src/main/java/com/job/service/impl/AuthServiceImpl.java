package com.job.service.impl;

import com.job.dao.entity.OtpDetails;
import com.job.dao.entity.User;
import com.job.dao.repository.OtpDetailsRepository;
import com.job.dao.repository.UserRepository;
import com.job.exception.custom.AlreadyExistException;
import com.job.exception.custom.InvalidInputException;
import com.job.exception.custom.NotFoundException;
import com.job.model.dto.request.LoginUserRequest;
import com.job.model.dto.request.OtpCodeRequest;
import com.job.model.dto.request.RefreshTokenRequest;
import com.job.model.dto.request.RegistrationRequest;
import com.job.model.dto.request.ResetPasswordRequest;
import com.job.model.dto.request.SendResetPasswordRequest;
import com.job.model.dto.response.AuthResponse;
import com.job.model.enums.UserRole;
import com.job.security.jwt.JwtService;
import com.job.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.job.exception.constant.ErrorCode.ALREADY_EXIST;
import static com.job.exception.constant.ErrorCode.INVALID_INPUT;
import static com.job.exception.constant.ErrorCode.NOT_FOUND;
import static com.job.exception.constant.ErrorMessage.ALREADY_EXIST_MESSAGE;
import static com.job.exception.constant.ErrorMessage.INVALID_INPUT_MESSAGE;
import static com.job.exception.constant.ErrorMessage.NOT_FOUND_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final OtpDetailsRepository otpDetailsRepository;

    @Override
    public void register(RegistrationRequest request) {
        log.info("Registering user");

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistException(String.format(ALREADY_EXIST_MESSAGE, "User"), ALREADY_EXIST);
        }

        String otp = otpService.generateOtp(request);

        String from = "jobapp@gmail.com";
        String to = request.getEmail();
        String subject = "Verify your account";
        String body = "Your otp code:" + otp;

        emailService.sendEmail(from, to, subject, body);
    }

    @Override
    @Transactional
    public AuthResponse verifyEmail(OtpCodeRequest otpRequest) {
        log.info("Verifying email, email {}", otpRequest.getEmail());
        if (!otpService.verifyOtp(otpRequest.getEmail(), otpRequest.getOtp())) {
            throw new InvalidInputException(String.format(INVALID_INPUT_MESSAGE, "otp code"),
                    INVALID_INPUT);
        }

        OtpDetails otpDetails = otpService.findLatestOtpDetails(otpRequest.getEmail());

        User user = User.builder()
                .email(otpDetails.getEmail())
                .firstName(otpDetails.getFirstName())
                .userRole(UserRole.USER)
                .password(otpDetails.getHashedPassword())
                .lastName(otpDetails.getLastName())
                .isActive(true)
                .build();

        userRepository.save(user);
        otpService.deleteAllOtpByEmail(user.getEmail());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse login(LoginUserRequest authRequest) {
        String email = authRequest.getEmail();
        String password = authRequest.getPassword();
        log.info("User login, email {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_MESSAGE, "User"),
                        NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidInputException(String.format(INVALID_INPUT_MESSAGE, "password"),
                    INVALID_INPUT);
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);


        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void logout() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User logout, userEmail {}", email);
        jwtService.invalidateToken(email);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        log.info("Refresh refresh token for userEmail {}", request.getEmail());
        if (jwtService.isRefreshTokenValid(request.getEmail(), request.getRefreshToken())) {
            throw new InvalidInputException(String.format(INVALID_INPUT_MESSAGE, "refresh token"),
                    INVALID_INPUT);
        }

        String userEmail = jwtService.extractUserEmail(request.getRefreshToken());

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_MESSAGE, "User"),
                        NOT_FOUND));

        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse
                .builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .build();
    }

    @Override
    public void sendOtpToEmail(SendResetPasswordRequest resetPasswordRequest){
        log.info("Send otp to email, userEmail {}", resetPasswordRequest.getEmail());
        otpService.generateOtp(resetPasswordRequest.getEmail());
        String otp = otpService.findLatestOtpCode(resetPasswordRequest.getEmail());

        String from = "jobapp@gmail.com";
        String to = resetPasswordRequest.getEmail();
        String subject = "Verify your account";
        String body = "Your otp code:" + otp;

        emailService.sendEmail(from, to, subject, body);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest resetPasswordRequest){
        log.info("Reset password, userEmail {}", resetPasswordRequest.getEmail());
        String email = resetPasswordRequest.getEmail();
        String password = resetPasswordRequest.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_MESSAGE, "User"),
                        NOT_FOUND));

        OtpDetails otpDetails = otpService.findLatestOtpDetails(email);

        if (!otpDetails.getCode().equals(resetPasswordRequest.getOtpCode()) ||
                otpDetails.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidInputException(String.format(INVALID_INPUT_MESSAGE, "Otp code"),
                    INVALID_INPUT);
        }

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        otpService.deleteAllOtpByEmail(email);
    }

}
