package com.gdghajithon.image;

import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png"
    );

    private final Path uploadDirectory;

    public ImageService(@Value("${app.upload.dir:uploads}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String save(MultipartFile file) {
        validate(file);

        String extension = EXTENSIONS.get(file.getContentType());
        String fileName = UUID.randomUUID() + extension;
        Path target = uploadDirectory.resolve(fileName);

        try {
            Files.createDirectories(uploadDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return fileName;
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.IMAGE_TOO_LARGE);
        }
        if (!EXTENSIONS.containsKey(file.getContentType()) || !hasValidSignature(file)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE);
        }
    }

    private boolean hasValidSignature(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(8);
            return switch (file.getContentType()) {
                case "image/jpeg" -> isJpeg(header);
                case "image/png" -> isPng(header);
                default -> false;
            };
        } catch (IOException exception) {
            return false;
        }
    }

    private boolean isJpeg(byte[] header) {
        return header.length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] header) {
        int[] pngSignature = {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        if (header.length < pngSignature.length) {
            return false;
        }
        for (int index = 0; index < pngSignature.length; index++) {
            if ((header[index] & 0xFF) != pngSignature[index]) {
                return false;
            }
        }
        return true;
    }
}
