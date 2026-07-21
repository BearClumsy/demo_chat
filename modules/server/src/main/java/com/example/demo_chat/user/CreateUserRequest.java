package com.example.demo_chat.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for creating a {@link User}. {@code @Size} limits mirror the DB column widths. */
public record CreateUserRequest(
    @NotBlank(message = "{user.firstName.required}")
        @Size(max = 100, message = "{user.firstName.size}")
        String firstName,
    @NotBlank(message = "{user.lastName.required}")
        @Size(max = 100, message = "{user.lastName.size}")
        String lastName,
    @NotBlank(message = "{user.email.required}")
        @Email(message = "{user.email.invalid}")
        @Size(max = 255, message = "{user.email.size}")
        String email,
    @Size(max = 20, message = "{user.phone.size}") String phone,
    @NotBlank(message = "{user.login.required}") @Size(max = 100, message = "{user.login.size}")
        String login,
    @NotBlank(message = "{user.password.required}")
        @Size(min = 8, max = 255, message = "{user.password.size}")
        String password) {}
