package com.gdghajithon.appointment;

import com.gdghajithon.appointment.dto.AppointmentCreateRequest;
import com.gdghajithon.appointment.dto.AppointmentCreateResponse;
import com.gdghajithon.appointment.dto.AppointmentListResponse;
import com.gdghajithon.appointment.dto.AppointmentUpdateRequest;
import com.gdghajithon.appointment.dto.AppointmentUpdateResponse;
import com.gdghajithon.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Appointment", description = "약속 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(summary = "약속 생성")
    @PostMapping("/friends/{friendId}/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentCreateResponse create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long friendId,
            @Valid @RequestBody AppointmentCreateRequest request
    ) {
        return appointmentService.create(authenticatedUser.userId(), friendId, request);
    }

    @Operation(summary = "내 약속 목록 조회")
    @GetMapping("/friends/{friendId}/appointments")
    public List<AppointmentListResponse> getAppointments(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long friendId
    ) {
        return appointmentService.getAppointments(authenticatedUser.userId(), friendId);
    }

    @Operation(summary = "약속 수정")
    @PatchMapping("/appointments/{appointmentId}")
    public AppointmentUpdateResponse update(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long appointmentId,
            @Valid @RequestBody AppointmentUpdateRequest request
    ) {
        return appointmentService.update(authenticatedUser.userId(), appointmentId, request);
    }

    @Operation(summary = "약속 삭제")
    @DeleteMapping("/appointments/{appointmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long appointmentId
    ) {
        appointmentService.delete(authenticatedUser.userId(), appointmentId);
    }
}
