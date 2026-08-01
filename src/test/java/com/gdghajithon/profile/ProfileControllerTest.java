package com.gdghajithon.profile;

import com.gdghajithon.global.security.JwtTokenProvider;
import com.gdghajithon.profile.dto.MyProfileResponse;
import com.gdghajithon.profile.dto.ProfileCreateRequest;
import com.gdghajithon.profile.dto.ProfileUpdateRequest;
import com.gdghajithon.profile.dto.UserDetailResponse;
import com.gdghajithon.profile.dto.UserRecommendationResponse;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ProfileService profileService;

    private String authorization;

    @BeforeEach
    void setUp() {
        authorization = "Bearer " + jwtTokenProvider.createToken(1L);
    }

    @Test
    void unauthenticatedRequestReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void createsProfile() throws Exception {
        when(profileService.create(anyLong(), any(ProfileCreateRequest.class)))
                .thenReturn(myProfileResponse());

        mockMvc.perform(post("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType("application/json")
                        .content(validRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("사용자"));
    }

    @Test
    void getsMyProfile() throws Exception {
        when(profileService.getMyProfile(1L)).thenReturn(myProfileResponse());

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("user01"));
    }

    @Test
    void updatesProfile() throws Exception {
        when(profileService.update(anyLong(), any(ProfileUpdateRequest.class)))
                .thenReturn(myProfileResponse());

        mockMvc.perform(patch("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType("application/json")
                        .content(validRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sportName").value("러닝"));
    }

    @Test
    void getsUserDetail() throws Exception {
        when(profileService.getUserDetail(1L, 2L)).thenReturn(userDetailResponse());

        mockMvc.perform(get("/api/users/2")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.friendStatus").value("NONE"));
    }

    @Test
    void recommendationsPathDoesNotConflictWithUserIdPath() throws Exception {
        when(profileService.getRecommendations(1L, null, null))
                .thenReturn(List.of(recommendationResponse()));

        mockMvc.perform(get("/api/users/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(2));
        verify(profileService).getRecommendations(1L, null, null);
    }

    @Test
    void validationFailureReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void invalidEnumReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name":"사용자",
                                  "age":25,
                                  "gender":"UNKNOWN",
                                  "sportId":1,
                                  "exerciseLevel":"INTERMEDIATE",
                                  "regionId":1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해주세요."));
    }

    private String validRequestJson() {
        return """
                {
                  "name":"사용자",
                  "age":25,
                  "gender":"MALE",
                  "sportId":1,
                  "exerciseLevel":"INTERMEDIATE",
                  "regionId":1
                }
                """;
    }

    private MyProfileResponse myProfileResponse() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 1, 12, 0);
        return new MyProfileResponse(
                1L, "user01", "사용자", 25, Gender.MALE,
                1L, "러닝", ExerciseLevel.INTERMEDIATE,
                1L, "강남구", 0, now, now);
    }

    private UserDetailResponse userDetailResponse() {
        return new UserDetailResponse(
                2L, "상대", 26, Gender.FEMALE,
                1L, "러닝", ExerciseLevel.BEGINNER,
                1L, "강남구", 0, FriendStatus.NONE, null, null);
    }

    private UserRecommendationResponse recommendationResponse() {
        return new UserRecommendationResponse(
                2L, "추천", 27, Gender.FEMALE,
                1L, "러닝", ExerciseLevel.INTERMEDIATE,
                1L, "강남구");
    }
}
