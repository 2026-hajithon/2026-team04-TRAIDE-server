package com.gdghajithon.image;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class ImageUrlResolverTest {

    private final ImageUrlResolver imageUrlResolver = new ImageUrlResolver();

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void relativePathBecomesAbsoluteUrlForCurrentServer() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("192.168.0.10");
        request.setServerPort(8080);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String imageUrl = imageUrlResolver.resolve("/images/sports/running.png");

        assertThat(imageUrl)
                .isEqualTo("http://192.168.0.10:8080/images/sports/running.png");
    }

    @Test
    void absoluteUrlIsKept() {
        String imageUrl = imageUrlResolver.resolve(
                "https://cdn.example.com/images/sports/running.png");

        assertThat(imageUrl)
                .isEqualTo("https://cdn.example.com/images/sports/running.png");
    }

    @Test
    void missingImageUrlReturnsNull() {
        assertThat(imageUrlResolver.resolve(null)).isNull();
    }
}
