package com.job.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationRequest {

    @Pattern(regexp = "^[a-zA-ZəƏıIğĞöÖşŞçÇ]{1,40}$", message = "Invalid name. Name can contain maximum 40 characters and only letters")
    private String firstName;

    @Size(max = 80, message = "Lastname can contain maximum 80 characters")
    @NotBlank
    private String lastName;

    @Email(message = "Invalid email")
    private String email;

    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$" , message = "Password must contain at lest 1 uppercase letter and one special character, one digit")
    private String password;
}
