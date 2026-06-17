package net.orderzone.idcard.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class PhotoService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    @Value("${app.photo.upload-dir:uploads/photos}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    void init() throws IOException {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPEG and PNG files are allowed");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File size must not exceed 5MB");
        }

        String extension = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> "";
        };

        String filename = UUID.randomUUID() + extension;
        try {
            Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + filename, e);
        }
        return filename;
    }

    public Resource load(String filename) {
        try {
            Path file = uploadPath.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("File not found or not readable: " + filename);
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + filename, e);
        }
    }

    public byte[] loadBytes(String filename) {
        try (var is = load(filename).getInputStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filename, e);
        }
    }
}
