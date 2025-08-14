package com.job.service;

import com.job.model.dto.request.LoginUserRequest;
import com.job.model.dto.request.OtpCodeRequest;
import com.job.model.dto.request.RefreshTokenRequest;
import com.job.model.dto.request.RegistrationRequest;
import com.job.model.dto.request.ResetPasswordRequest;
import com.job.model.dto.request.SendResetPasswordRequest;
import com.job.model.dto.response.AuthResponse;

public interface AuthService {

    void register(RegistrationRequest request);

    AuthResponse verifyEmail(OtpCodeRequest otpRequest);

    AuthResponse login(LoginUserRequest authRequest);

    void logout();

    AuthResponse refresh(RefreshTokenRequest request);

    void sendOtpToEmail(SendResetPasswordRequest resetPasswordRequest);

    void resetPassword(ResetPasswordRequest resetPasswordRequest);

}
