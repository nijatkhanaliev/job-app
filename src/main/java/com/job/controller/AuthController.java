package com.job.controller;

import com.job.model.dto.request.LoginUserRequest;
import com.job.model.dto.request.OtpCodeRequest;
import com.job.model.dto.request.RefreshTokenRequest;
import com.job.model.dto.request.RegistrationRequest;
import com.job.model.dto.request.ResetPasswordRequest;
import com.job.model.dto.request.SendResetPasswordRequest;
import com.job.model.dto.response.AuthResponse;
import com.job.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegistrationRequest request) {
        authService.register(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestBody @Valid OtpCodeRequest request) {
        return ResponseEntity.status(CREATED)
                .body(authService.verifyEmail(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginUserRequest request) {
        return ResponseEntity.status(OK)
                .body(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send-reset-password-code")
    public ResponseEntity<Void> sendOtpToEmail(@RequestBody @Valid SendResetPasswordRequest request) {
        authService.sendOtpToEmail(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> sendOtpToEmail(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

}
