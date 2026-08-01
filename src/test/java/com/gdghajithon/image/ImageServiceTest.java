package com.gdghajithon.image;

import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageServiceTest {

    @TempDir
    Path uploadDirectory;

    @Test
    void jpegImageCanBeSaved() throws IOException {
        ImageService imageService = new ImageService(uploadDirectory.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}
        );

        String fileName = imageService.save(file);

        assertThat(fileName).endsWith(".jpg");
        assertThat(Files.exists(uploadDirectory.resolve(fileName))).isTrue();
    }

    @Test
    void pngImageCanBeSaved() throws IOException {
        ImageService imageService = new ImageService(uploadDirectory.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "review.png",
                "image/png",
                new byte[]{
                        (byte) 0x89, 0x50, 0x4E, 0x47,
                        0x0D, 0x0A, 0x1A, 0x0A
                }
        );

        String fileName = imageService.save(file);

        assertThat(fileName).endsWith(".png");
        assertThat(Files.exists(uploadDirectory.resolve(fileName))).isTrue();
    }

    @Test
    void emptyFileCannotBeSaved() {
        ImageService imageService = new ImageService(uploadDirectory.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        assertThatThrownBy(() -> imageService.save(file))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_IMAGE);
    }

    @Test
    void unsupportedImageCannotBeSaved() {
        ImageService imageService = new ImageService(uploadDirectory.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.gif",
                "image/gif",
                new byte[]{0x47, 0x49, 0x46}
        );

        assertThatThrownBy(() -> imageService.save(file))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_IMAGE);
    }

    @Test
    void fakePngCannotBeSaved() {
        ImageService imageService = new ImageService(uploadDirectory.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.png",
                "image/png",
                new byte[]{0x47, 0x49, 0x46}
        );

        assertThatThrownBy(() -> imageService.save(file))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_IMAGE);
    }

    @Test
    void imageLargerThanFiveMegabytesCannotBeSaved() {
        ImageService imageService = new ImageService(uploadDirectory.toString());
        byte[] content = new byte[5 * 1024 * 1024 + 1];
        content[0] = (byte) 0xFF;
        content[1] = (byte) 0xD8;
        content[2] = (byte) 0xFF;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                content
        );

        assertThatThrownBy(() -> imageService.save(file))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.IMAGE_TOO_LARGE);
    }
}
