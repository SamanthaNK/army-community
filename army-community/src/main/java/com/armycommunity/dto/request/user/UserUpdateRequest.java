package com.armycommunity.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;

    private String profileImagePath;

    private String languagePreference;
    private String timeZone;
}
