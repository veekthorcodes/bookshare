package com.codes.bookshare.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegistrationRequest {

    @NotNull(message = "First name is required")
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotNull(message = "Last name is required")
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email address")
    @NotNull(message = "Email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 6, message = "Password should be at least 6 characters")
    @NotNull(message = "Password is required")
    @NotBlank(message = "Password is required")
    private String password;
}
