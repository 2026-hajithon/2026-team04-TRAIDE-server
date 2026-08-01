package com.gdghajithon.appointment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentQueryService {

    private final AppointmentRepository appointmentRepository;

    public long countByUserId(Long userId) {
        return appointmentRepository.countByUserId(userId);
    }

    public long countBetweenUsers(Long userId, Long friendId) {
        return appointmentRepository.countBetweenUsers(userId, friendId);
    }

    public boolean existsBetweenUsers(Long userId, Long friendId) {
        return countBetweenUsers(userId, friendId) > 0;
    }
}
