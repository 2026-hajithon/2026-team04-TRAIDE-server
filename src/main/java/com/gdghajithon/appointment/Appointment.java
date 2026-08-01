package com.gdghajithon.appointment;

import com.gdghajithon.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "appointment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false, length = 100)
    private String place;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Appointment(User creator, User friend, LocalDateTime dateTime, String place) {
        this.creator = creator;
        this.friend = friend;
        this.dateTime = dateTime;
        this.place = place;
    }

    public static Appointment create(
            User creator,
            User friend,
            LocalDateTime dateTime,
            String place
    ) {
        return new Appointment(creator, friend, dateTime, place);
    }

    public void update(LocalDateTime dateTime, String place) {
        if (dateTime != null) {
            this.dateTime = dateTime;
        }
        if (place != null) {
            this.place = place;
        }
    }

    public boolean hasParticipant(Long userId) {
        return creator.getId().equals(userId) || friend.getId().equals(userId);
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
