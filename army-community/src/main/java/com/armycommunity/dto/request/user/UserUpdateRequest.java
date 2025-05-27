package com.armycommunity.dto.request.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    @Size(max = 10, message = "Language preference must not exceed 10 characters")
    private String languagePreference;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    private MultipartFile profileImage;

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String currentPassword;

    @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
    private String newPassword;

    private String confirmNewPassword;

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordMatching() {
        if (newPassword == null) return true; // Password update is optional
        return newPassword.equals(confirmNewPassword);
    }
}
