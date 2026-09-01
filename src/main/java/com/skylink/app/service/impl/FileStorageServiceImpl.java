package com.skylink.app.service.impl;

import com.skylink.app.service.IFileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
@Profile("!prod")
@Slf4j
public class FileStorageServiceImpl implements IFileStorageService {

    @Value("${app.upload.base-path:uploads/}")
    private String basePath;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    @Override
    public String store(MultipartFile file, String subfolder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Cannot store empty file");
        }
        if (!isValidImageFile(file)) {
            throw new IOException("Invalid file type. Allowed: JPG, PNG, WEBP");
        }

        Path uploadDir = resolveDirectory(subfolder);
        Files.createDirectories(uploadDir);
        String filename = UUID.randomUUID() + "." + getExtension(file.getOriginalFilename());
        Path destination = uploadDir.resolve(filename).normalize();
        if (!destination.startsWith(uploadDir)) {
            throw new IOException("Invalid upload path");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        String relativePath = subfolder + "/" + filename;
        log.info("Stored file: {}", relativePath);
        return relativePath;
    }

    @Override
    public Resource loadAsResource(String filename, String subfolder) {
        try {
            Path directory = resolveDirectory(subfolder);
            Path file = directory.resolve(filename).normalize();
            if (!file.startsWith(directory)) {
                throw new IllegalArgumentException("Invalid file path");
            }
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new IllegalArgumentException("File not readable: " + filename);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed file path: " + filename, e);
        }
    }

    @Override
    public void delete(String filename, String subfolder) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        try {
            Path directory = resolveDirectory(subfolder);
            Path file = directory.resolve(filename).normalize();
            if (!file.startsWith(directory)) {
                log.warn("Refused to delete file outside upload directory: {}", filename);
                return;
            }
            if (Files.deleteIfExists(file)) {
                log.info("Deleted file: {}/{}", subfolder, filename);
            }
        } catch (IOException | InvalidPathException e) {
            log.warn("Could not delete file: {}/{} - {}", subfolder, filename, e.getMessage());
        }
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return false;
        }
        return ALLOWED_EXTENSIONS.contains(getExtension(file.getOriginalFilename()));
    }

    private Path resolveDirectory(String subfolder) {
        Path baseDirectory = Paths.get(basePath).toAbsolutePath().normalize();
        Path directory = baseDirectory.resolve(subfolder).normalize();
        if (!directory.startsWith(baseDirectory)) {
            throw new IllegalArgumentException("Invalid upload subfolder");
        }
        return directory;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
