package com.gdghajithon.profile;

import com.gdghajithon.region.Region;
import com.gdghajithon.sport.Sport;
import com.gdghajithon.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "profile",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_profile_user",
                columnNames = "user_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExerciseLevel exerciseLevel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Profile(
            User user,
            String name,
            Integer age,
            Gender gender,
            Sport sport,
            ExerciseLevel exerciseLevel,
            Region region
    ) {
        this.user = user;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.sport = sport;
        this.exerciseLevel = exerciseLevel;
        this.region = region;
    }

    public static Profile create(
            User user,
            String name,
            Integer age,
            Gender gender,
            Sport sport,
            ExerciseLevel exerciseLevel,
            Region region
    ) {
        return new Profile(user, name, age, gender, sport, exerciseLevel, region);
    }

    public void update(
            String name,
            Integer age,
            Gender gender,
            Sport sport,
            ExerciseLevel exerciseLevel,
            Region region
    ) {
        boolean changed = false;
        if (name != null) {
            this.name = name;
            changed = true;
        }
        if (age != null) {
            this.age = age;
            changed = true;
        }
        if (gender != null) {
            this.gender = gender;
            changed = true;
        }
        if (sport != null) {
            this.sport = sport;
            changed = true;
        }
        if (exerciseLevel != null) {
            this.exerciseLevel = exerciseLevel;
            changed = true;
        }
        if (region != null) {
            this.region = region;
            changed = true;
        }
        if (changed) {
            this.updatedAt = LocalDateTime.now();
        }
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
