package com.armycommunity.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageConfig.class);
    private static final String[] UPLOAD_SUBDIRS = {"profile", "post", "album", "member"};

    @Value ("${file.upload-dir}")
    private String uploadDir;

    @Bean
    public Path createUploadDirectoryIfNotExists() throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath);
            }

            for (String subdir : UPLOAD_SUBDIRS) {
                createSubdirectory(uploadPath, subdir);
            }

            return uploadPath;
        } catch (IOException e) {
            logger.error("Could not create upload directories: {}", e.getMessage());
            throw new IOException("Failed to create upload directories", e);
        }
    }

    private void createSubdirectory(Path parent, String subdirectory) throws IOException {
        Path subPath = parent.resolve(subdirectory);
        if (!Files.exists(subPath)) {
            Files.createDirectories(subPath);
            logger.info("Created subdirectory: {}", subPath);
        }
    }
}
