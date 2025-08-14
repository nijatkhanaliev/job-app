package com.job.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpCodeRequest {
    @Size(min = 6, max = 6, message = "Otp code size must be 6")
    private String otp;

    @Email(message = "Invalid email")
    private String email;
}
