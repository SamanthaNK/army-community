package com.armycommunity.service.filestorage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {
    String storeFile(MultipartFile file, String subDirectory) throws IOException;

    Resource loadFileAsResource(String filePath);

    void deleteFile(String filePath) throws IOException;

    Path getFilePath(String filename, String directory);

    String getStorageDirectory();
}
