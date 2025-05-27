package com.armycommunity.dto.request.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileImageUploadRequest {
    @NotNull(message = "Profile image file is required")
    private MultipartFile profileImage;

    @AssertTrue(message = "Profile image file cannot be empty")
    public boolean isFileNotEmpty() {
        return profileImage != null && !profileImage.isEmpty();
    }

    @AssertTrue(message = "Profile image must be an image file (jpg, jpeg, png, gif)")
    public boolean isValidImageType() {
        if (profileImage == null || profileImage.isEmpty()) {
            return false;
        }
        String contentType = profileImage.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif")
        );
    }

    @AssertTrue(message = "Profile image size must not exceed 5MB")
    public boolean isValidFileSize() {
        if (profileImage == null || profileImage.isEmpty()) {
            return false;
        }
        return profileImage.getSize() <= 5 * 1024 * 1024; // 5MB limit
    }
}
