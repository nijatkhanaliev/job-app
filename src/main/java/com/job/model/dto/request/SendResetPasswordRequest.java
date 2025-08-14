package com.job.model.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendResetPasswordRequest {
    @Email(message = "Invalid email")
    private String email;
}
