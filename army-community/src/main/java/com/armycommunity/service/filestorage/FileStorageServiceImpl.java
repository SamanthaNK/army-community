package com.armycommunity.service.filestorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.file.storage.path:uploads}")
    private String storageBasePath;

    @Value("${app.file.max-size:10485760}") // 10MB default
    private long maxFileSize;

    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Override
    public String storeFile(MultipartFile file, String directory) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Cannot store empty file");
        }

        if (file.getSize() > maxFileSize) {
            throw new IOException("File size exceeds maximum allowed size");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (!isValidFileType(originalFilename)) {
            throw new IOException("File type not allowed: " + originalFilename);
        }

        try {
            // Create unique filename with timestamp and UUID
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = generateUniqueFilename(fileExtension);

            // Create date-based directory structure
            String dateDirectory = LocalDateTime.now().format(DATE_FORMATTER);
            Path targetLocation = getStorageDirectory()
                    .resolve(directory)
                    .resolve(dateDirectory)
                    .resolve(uniqueFilename);

            // Create directories if they don't exist
            Files.createDirectories(targetLocation.getParent());

            // Copy file to target location
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = directory + "/" + dateDirectory + "/" + uniqueFilename;
            log.info("File stored successfully: {}", relativePath);

            return relativePath;

        } catch (IOException ex) {
            log.error("Failed to store file {}: {}", originalFilename, ex.getMessage());
            throw new IOException("Failed to store file " + originalFilename, ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        try {
            Path file = getStorageDirectory().resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                log.warn("File not found or not readable: {}", filePath);
                throw new RuntimeException("File not found: " + filePath);
            }
        } catch (MalformedURLException ex) {
            log.error("Malformed URL for file path {}: {}", filePath, ex.getMessage());
            throw new RuntimeException("File not found: " + filePath, ex);
        }
    }

    @Override
    public void deleteFile(String filePath) throws IOException {
        try {
            Path file = getStorageDirectory().resolve(filePath).normalize();

            if (Files.exists(file)) {
                Files.delete(file);
                log.info("File deleted successfully: {}", filePath);
            } else {
                log.warn("Attempted to delete non-existent file: {}", filePath);
            }
        } catch (IOException ex) {
            log.error("Failed to delete file {}: {}", filePath, ex.getMessage());
            throw new IOException("Failed to delete file: " + filePath, ex);
        }
    }

    @Override
    public String getFilePath(String filename, String directory) {
        return directory + "/" + filename;
    }

    @Override
    public Path getStorageDirectory() {
        Path path = Paths.get(storageBasePath).normalize().toAbsolutePath();

        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Created storage directory: {}", path);
            }
        } catch (IOException ex) {
            log.error("Failed to create storage directory {}: {}", path, ex.getMessage());
            throw new RuntimeException("Could not create storage directory", ex);
        }

        return path;
    }

    private boolean isValidFileType(String filename) {
        if (filename == null) return false;

        String lowerCaseFilename = filename.toLowerCase();
        for (String extension : ALLOWED_EXTENSIONS) {
            if (lowerCaseFilename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String generateUniqueFilename(String extension) {
        return UUID.randomUUID().toString() + extension;
    }

}
