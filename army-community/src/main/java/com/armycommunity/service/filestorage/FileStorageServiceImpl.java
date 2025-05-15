package com.armycommunity.service.filestorage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        // Create the target directory if it doesn't exist
        Path targetDir = Paths.get(uploadDir, subDirectory).toAbsolutePath().normalize();
        Files.createDirectories(targetDir);

        // Generate a unique filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID().toString() +
                originalFilename.substring(originalFilename.lastIndexOf("."));

        // Copy the file to the target location
        Path targetLocation = targetDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Return the relative path for storing in the database
        return subDirectory + "/" + uniqueFilename;
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        try {
            Path fileLocation = Paths.get(uploadDir, filePath).toAbsolutePath().normalize();
            Resource resource = new UrlResource(fileLocation.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found: " + filePath, ex);
        }
    }

    @Override
    public void deleteFile(String filePath) throws IOException {
        if (filePath != null && !filePath.isEmpty()) {
            Path targetLocation = Paths.get(uploadDir, filePath).toAbsolutePath().normalize();
            Files.deleteIfExists(targetLocation);
        }
    }

    @Override
    public Path getFilePath(String filename, String directory) {
        return Paths.get(uploadDir, directory, filename).toAbsolutePath().normalize();
    }

    @Override
    public String getStorageDirectory() {
        return uploadDir;
    }

}
