package com.gdghajithon.review;

import com.gdghajithon.global.security.JwtTokenProvider;
import com.gdghajithon.review.dto.ReviewCreateRequest;
import com.gdghajithon.review.dto.ReviewCreateResponse;
import com.gdghajithon.review.dto.ReviewListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ReviewService reviewService;

    private String authorization;

    @BeforeEach
    void setUp() {
        authorization = "Bearer " + jwtTokenProvider.createToken(1L);
    }

    @Test
    void unauthenticatedRequestReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/2/reviews"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void createsReview() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 2, 12, 0);
        when(reviewService.create(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(2L), any(ReviewCreateRequest.class)))
                .thenReturn(new ReviewCreateResponse(
                        10L, 5, "즐거웠어요", "https://example.com/review.jpg", createdAt));

        mockMvc.perform(post("/api/users/2/reviews")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType("application/json")
                        .content("""
                                {
                                  "rating": 5,
                                  "content": "즐거웠어요",
                                  "imageUrl": "https://example.com/review.jpg"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.content").value("즐거웠어요"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/review.jpg"));

        verify(reviewService).create(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(2L),
                any(ReviewCreateRequest.class)
        );
    }

    @Test
    void getsReceivedReviews() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 2, 12, 0);
        when(reviewService.getReviews(2L)).thenReturn(List.of(
                new ReviewListResponse(
                        10L,
                        5,
                        "즐거웠어요",
                        null,
                        createdAt,
                        new ReviewListResponse.WriterResponse(
                                1L,
                                "작성자",
                                "https://api.example.com/images/sports/running.png"
                        )
                )
        ));

        mockMvc.perform(get("/api/users/2/reviews")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].writer.id").value(1))
                .andExpect(jsonPath("$[0].writer.name").value("작성자"))
                .andExpect(jsonPath("$[0].writer.imageUrl")
                        .value("https://api.example.com/images/sports/running.png"));
    }
}
