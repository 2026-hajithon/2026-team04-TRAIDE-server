package com.gdghajithon.appointment.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AppointmentUpdateRequest {

    @Future
    private LocalDateTime dateTime;

    @Size(min = 1, max = 100)
    private String place;

    private Long coachId;
    private boolean placeIncluded;

    public AppointmentUpdateRequest() {
    }

    public AppointmentUpdateRequest(LocalDateTime dateTime, String place, Long coachId) {
        this(dateTime, place, coachId, place != null);
    }

    public AppointmentUpdateRequest(
            LocalDateTime dateTime,
            String place,
            Long coachId,
            boolean placeIncluded
    ) {
        this.dateTime = dateTime;
        this.place = place;
        this.coachId = coachId;
        this.placeIncluded = placeIncluded;
    }

    public LocalDateTime dateTime() {
        return dateTime;
    }

    public String place() {
        return place;
    }

    public Long coachId() {
        return coachId;
    }

    public boolean placeIncluded() {
        return placeIncluded;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @JsonSetter("place")
    public void setPlace(String place) {
        this.place = place;
        this.placeIncluded = true;
    }

    public void setCoachId(Long coachId) {
        this.coachId = coachId;
    }
}
