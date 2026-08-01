package com.gdghajithon.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
            SELECT appointment
            FROM Appointment appointment
            WHERE (appointment.creator.id = :userId AND appointment.friend.id = :friendId)
               OR (appointment.creator.id = :friendId AND appointment.friend.id = :userId)
            ORDER BY appointment.dateTime ASC
            """)
    List<Appointment> findAllBetweenUsers(
            @Param("userId") Long userId,
            @Param("friendId") Long friendId
    );
}
