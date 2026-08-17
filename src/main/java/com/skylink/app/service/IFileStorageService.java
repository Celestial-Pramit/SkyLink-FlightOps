package com.skylink.app.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IFileStorageService {
    String store(MultipartFile file, String subfolder) throws IOException;
    Resource loadAsResource(String filename, String subfolder);
    void delete(String filename, String subfolder);
    boolean isValidImageFile(MultipartFile file);
}
