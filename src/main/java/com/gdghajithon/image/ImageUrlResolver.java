package com.gdghajithon.image;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Component
public class ImageUrlResolver {

    public String resolve(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        if (URI.create(imageUrl).isAbsolute()) {
            return imageUrl;
        }

        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(imageUrl)
                    .toUriString();
        } catch (IllegalStateException exception) {
            return imageUrl;
        }
    }
}
