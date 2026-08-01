package com.gdghajithon.profile;

import com.gdghajithon.global.security.JwtTokenProvider;
import com.gdghajithon.profile.dto.MyProfileResponse;
import com.gdghajithon.profile.dto.ProfileCreateRequest;
import com.gdghajithon.profile.dto.ProfileUpdateRequest;
import com.gdghajithon.profile.dto.RegionSummaryResponse;
import com.gdghajithon.profile.dto.SportSummaryResponse;
import com.gdghajithon.profile.dto.UserDetailResponse;
import com.gdghajithon.profile.dto.UserRecommendationListResponse;
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
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.name").value("사용자"))
                .andExpect(jsonPath("$.imageUrl")
                        .value("https://api.example.com/images/sports/running.png"))
                .andExpect(jsonPath("$.sport.id").value(1))
                .andExpect(jsonPath("$.sport.name").value("러닝"))
                .andExpect(jsonPath("$.level").value("INTERMEDIATE"))
                .andExpect(jsonPath("$.exerciseLevel").doesNotExist())
                .andExpect(jsonPath("$.region.id").value(1))
                .andExpect(jsonPath("$.averageRating").isEmpty())
                .andExpect(jsonPath("$.reviewCount").value(0));
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
                .andExpect(jsonPath("$.sport.name").value("러닝"));
    }

    @Test
    void emptyPatchReturnsOk() throws Exception {
        when(profileService.update(anyLong(), any(ProfileUpdateRequest.class)))
                .thenReturn(myProfileResponse());

        mockMvc.perform(patch("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("사용자"));
    }

    @Test
    void patchRejectsBlankNameWhenProvided() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType("application/json")
                        .content("""
                                {"name":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void getsUserDetail() throws Exception {
        when(profileService.getUserDetail(1L, 2L)).thenReturn(userDetailResponse());

        mockMvc.perform(get("/api/users/2")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.imageUrl")
                        .value("https://api.example.com/images/sports/running.png"))
                .andExpect(jsonPath("$.friendStatus").value("NONE"))
                .andExpect(jsonPath("$.friendSinceDays").isEmpty())
                .andExpect(jsonPath("$.friendshipId").doesNotExist())
                .andExpect(jsonPath("$.friendSince").doesNotExist())
                .andExpect(jsonPath("$.relationship").doesNotExist());
    }

    @Test
    void recommendationsPathDoesNotConflictWithUserIdPath() throws Exception {
        when(profileService.getRecommendations(1L, List.of(1L, 2L), List.of(3L, 4L)))
                .thenReturn(UserRecommendationListResponse.of(List.of(recommendationResponse())));

        mockMvc.perform(get("/api/users/recommendations")
                        .queryParam("sportIds", "1,2")
                        .queryParam("regionIds", "3,4")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(2))
                .andExpect(jsonPath("$.items[0].imageUrl")
                        .value("https://api.example.com/images/sports/running.png"))
                .andExpect(jsonPath("$.items[0].averageRating").isEmpty())
                .andExpect(jsonPath("$.items[0].reviewCount").value(0));
        verify(profileService).getRecommendations(1L, List.of(1L, 2L), List.of(3L, 4L));
    }

    @Test
    void recommendationFiltersAreDocumentedAsCommaSeparatedLongArrays() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[0].name")
                        .value("sportIds"))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[0].in")
                        .value("query"))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[0].required")
                        .value(false))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[0].style")
                        .value("form"))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[0].explode")
                        .value(false))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[0].schema.type")
                        .value("array"))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[0].schema.items.type")
                        .value("integer"))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[0].schema.items.format")
                        .value("int64"))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[0].example[0]")
                        .value(2))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[0].example[1]")
                        .value(3))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[1].name")
                        .value("regionIds"))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[1].in")
                        .value("query"))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[1].required")
                        .value(false))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[1].style")
                        .value("form"))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[1].explode")
                        .value(false))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[1].schema.type")
                        .value("array"))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[1].schema.items.type")
                        .value("integer"))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[1].schema.items.format")
                        .value("int64"))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[1].example[0]")
                        .value(1))
                .andExpect(jsonPath("$.paths['/api/users/recommendations'].get.parameters[1].example[1]")
                        .value(19));
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
                                  "level":"INTERMEDIATE",
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
                  "level":"INTERMEDIATE",
                  "regionId":1
                }
                """;
    }

    private MyProfileResponse myProfileResponse() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 1, 12, 0);
        return new MyProfileResponse(
                1L, "user01", "사용자",
                "https://api.example.com/images/sports/running.png",
                25, Gender.MALE,
                new SportSummaryResponse(1L, "러닝"), ExerciseLevel.INTERMEDIATE,
                new RegionSummaryResponse(1L, "강남구"),
                0, 0, null, 0, now, now);
    }

    private UserDetailResponse userDetailResponse() {
        return new UserDetailResponse(
                2L, "상대", "https://api.example.com/images/sports/running.png",
                26, Gender.FEMALE,
                new SportSummaryResponse(1L, "러닝"), ExerciseLevel.BEGINNER,
                new RegionSummaryResponse(1L, "강남구"),
                0, 0, null, 0, FriendStatus.NONE, null);
    }

    private UserRecommendationResponse recommendationResponse() {
        return new UserRecommendationResponse(
                2L, "추천", "https://api.example.com/images/sports/running.png",
                27, Gender.FEMALE,
                new SportSummaryResponse(1L, "러닝"), ExerciseLevel.INTERMEDIATE,
                new RegionSummaryResponse(1L, "강남구"), null, 0);
    }
}
