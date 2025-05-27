package com.armycommunity.dto.request.user;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    @Size(max = 10, message = "Language preference must not exceed 10 characters")
    private String languagePreference = "en";

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone = "UTC";

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
