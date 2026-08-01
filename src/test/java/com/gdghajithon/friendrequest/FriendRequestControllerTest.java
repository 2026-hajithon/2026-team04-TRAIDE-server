package com.gdghajithon.friendrequest;

import com.gdghajithon.friendrequest.dto.FriendRequestCreateRequest;
import com.gdghajithon.friendrequest.dto.FriendRequestCreateResponse;
import com.gdghajithon.friendrequest.dto.FriendRequestItemResponse;
import com.gdghajithon.friendrequest.dto.FriendRequestListResponse;
import com.gdghajithon.friendrequest.dto.FriendRequestStatusResponse;
import com.gdghajithon.friendrequest.dto.FriendRequestUserResponse;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.global.security.JwtTokenProvider;
import com.gdghajithon.profile.ExerciseLevel;
import com.gdghajithon.profile.Gender;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FriendRequestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private FriendRequestService friendRequestService;

    private String authorization;

    @BeforeEach
    void setUp() {
        authorization = "Bearer " + jwtTokenProvider.createToken(1L);
    }

    @Test
    void unauthenticatedRequestReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/friend-requests/received"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createsRequestWithCreatedStatus() throws Exception {
        when(friendRequestService.create(anyLong(), any(FriendRequestCreateRequest.class)))
                .thenReturn(new FriendRequestCreateResponse(
                        101L, 1L, 35L, FriendRequestStatus.PENDING,
                        LocalDateTime.of(2026, 8, 2, 10, 15)));

        mockMvc.perform(post("/api/friend-requests")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType("application/json")
                        .content("{\"receiverUserId\":35}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").value(101))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void returnsReceivedAndSentWrappers() throws Exception {
        FriendRequestListResponse response = listResponse();
        when(friendRequestService.getReceived(1L)).thenReturn(response);
        when(friendRequestService.getSent(1L)).thenReturn(response);

        mockMvc.perform(get("/api/friend-requests/received")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].requestId").value(101))
                .andExpect(jsonPath("$.items[0].user.sport.name").value("테니스"));
        mockMvc.perform(get("/api/friend-requests/sent")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].user.region.name").value("강남구"));
    }

    @Test
    void acceptsAndRejectsRequest() throws Exception {
        when(friendRequestService.respond(anyLong(), anyLong(), any()))
                .thenReturn(new FriendRequestStatusResponse(101L, FriendRequestStatus.ACCEPTED))
                .thenReturn(new FriendRequestStatusResponse(102L, FriendRequestStatus.REJECTED));

        mockMvc.perform(patch("/api/friend-requests/101")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType("application/json")
                        .content("{\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
        mockMvc.perform(patch("/api/friend-requests/102")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType("application/json")
                        .content("{\"status\":\"REJECTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void pendingCannotBeUsedAsRespondStatus() throws Exception {
        mockMvc.perform(patch("/api/friend-requests/101")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType("application/json")
                        .content("{\"status\":\"PENDING\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void cancelsRequestWithNoContent() throws Exception {
        doNothing().when(friendRequestService).cancel(1L, 101L);

        mockMvc.perform(delete("/api/friend-requests/101")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isNoContent());
    }

    @Test
    void returnsBusinessErrorStatuses() throws Exception {
        when(friendRequestService.create(anyLong(), any()))
                .thenThrow(new BusinessException(ErrorCode.FRIEND_REQUEST_ALREADY_PENDING));
        doThrow(new BusinessException(ErrorCode.FRIEND_REQUEST_ACCESS_DENIED))
                .when(friendRequestService).cancel(1L, 101L);

        mockMvc.perform(post("/api/friend-requests")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType("application/json")
                        .content("{\"receiverUserId\":35}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FRIEND_REQUEST_ALREADY_PENDING"));
        mockMvc.perform(delete("/api/friend-requests/101")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FRIEND_REQUEST_ACCESS_DENIED"));
    }

    @Test
    void returnsNotFoundForMissingRequest() throws Exception {
        when(friendRequestService.respond(anyLong(), anyLong(), any()))
                .thenThrow(new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        mockMvc.perform(patch("/api/friend-requests/999")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType("application/json")
                        .content("{\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FRIEND_REQUEST_NOT_FOUND"));
    }

    private FriendRequestListResponse listResponse() {
        FriendRequestUserResponse user = new FriendRequestUserResponse(
                35L, "이서연", 24, Gender.FEMALE,
                new SportSummaryResponse(6L, "테니스"),
                ExerciseLevel.INTERMEDIATE,
                new RegionSummaryResponse(1L, "강남구"));
        return FriendRequestListResponse.of(List.of(new FriendRequestItemResponse(
                101L, LocalDateTime.of(2026, 8, 2, 10, 15), user)));
    }
}
