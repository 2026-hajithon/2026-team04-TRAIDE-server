package com.gdghajithon.friend;

import com.gdghajithon.friend.dto.FriendItemResponse;
import com.gdghajithon.friend.dto.FriendListResponse;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.global.security.JwtTokenProvider;
import com.gdghajithon.profile.dto.RegionSummaryResponse;
import com.gdghajithon.profile.dto.SportSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FriendControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private FriendService friendService;

    private String authorization;

    @BeforeEach
    void setUp() {
        authorization = "Bearer " + jwtTokenProvider.createToken(1L);
    }

    @Test
    void unauthenticatedListReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/friends"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsFriendListContract() throws Exception {
        when(friendService.getFriends(1L)).thenReturn(FriendListResponse.of(List.of(
                new FriendItemResponse(
                        2L,
                        "이서연",
                        new SportSummaryResponse(6L, "테니스"),
                        new RegionSummaryResponse(1L, "강남구"),
                        "http://localhost:8080/images/sports/tennis.png",
                        5L,
                        "1_2"
                ))));

        mockMvc.perform(get("/api/friends")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(2))
                .andExpect(jsonPath("$.items[0].name").value("이서연"))
                .andExpect(jsonPath("$.items[0].sport.id").value(6))
                .andExpect(jsonPath("$.items[0].sport.name").value("테니스"))
                .andExpect(jsonPath("$.items[0].region.id").value(1))
                .andExpect(jsonPath("$.items[0].region.name").value("강남구"))
                .andExpect(jsonPath("$.items[0].imageUrl")
                        .value("http://localhost:8080/images/sports/tennis.png"))
                .andExpect(jsonPath("$.items[0].appointmentCount").value(5))
                .andExpect(jsonPath("$.items[0].chatRoomId").value("1_2"))
                .andExpect(jsonPath("$.items[0].friendshipId").doesNotExist())
                .andExpect(jsonPath("$.items[0].friendSinceDays").doesNotExist())
                .andExpect(jsonPath("$.items[0].friendStatus").doesNotExist());
    }

    @Test
    void returnsEmptyItems() throws Exception {
        when(friendService.getFriends(1L)).thenReturn(FriendListResponse.of(List.of()));

        mockMvc.perform(get("/api/friends")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void deletesFriendWithNoContent() throws Exception {
        doNothing().when(friendService).deleteFriend(1L, 2L);

        mockMvc.perform(delete("/api/friends/2")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void returnsNotFriendError() throws Exception {
        doThrow(new BusinessException(ErrorCode.NOT_FRIEND))
                .when(friendService).deleteFriend(1L, 2L);

        mockMvc.perform(delete("/api/friends/2")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_FRIEND"))
                .andExpect(jsonPath("$.message").value("친구가 아닙니다."));
    }

    @Test
    void exposesFriendListAndDeleteInOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/friends'].get").exists())
                .andExpect(jsonPath("$.paths['/api/friends/{friendUserId}'].delete").exists());
    }
}
