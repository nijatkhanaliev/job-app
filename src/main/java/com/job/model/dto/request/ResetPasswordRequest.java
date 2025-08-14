package com.job.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @Email(message = "Invalid email")
    private String email;

    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$" , message = "Password must contain at lest 1 uppercase letter and one special character, one digit")
    private String password;

    @Size(min = 6, max = 6)
    private String otpCode;
}
