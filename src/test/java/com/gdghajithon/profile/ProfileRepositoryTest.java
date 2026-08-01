package com.gdghajithon.profile;

import com.gdghajithon.friend.Friendship;
import com.gdghajithon.friend.FriendshipRepository;
import com.gdghajithon.region.Region;
import com.gdghajithon.region.RegionRepository;
import com.gdghajithon.sport.Sport;
import com.gdghajithon.sport.SportRepository;
import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class ProfileRepositoryTest {

    @Autowired private ProfileRepository profileRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SportRepository sportRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private EntityManagerFactory entityManagerFactory;

    @Test
    void findsProfileByUserId() {
        User user = saveUser("user01");
        Profile profile = saveProfile(user, saveSport("러닝"), saveRegion("강남구"), "사용자");

        assertThat(profileRepository.findByUserId(user.getId())).contains(profile);
        assertThat(profileRepository.existsByUserId(user.getId())).isTrue();
    }

    @Test
    void preventsDuplicateProfileForUser() {
        User user = saveUser("user01");
        Sport sport = saveSport("러닝");
        Region region = saveRegion("강남구");
        saveProfile(user, sport, region, "첫 프로필");

        assertThatThrownBy(() -> {
            profileRepository.saveAndFlush(Profile.create(
                    user, "두 번째", 25, Gender.MALE, sport,
                    ExerciseLevel.BEGINNER, region));
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void fetchesSportAndRegionWithProfile() {
        User user = saveUser("user01");
        saveProfile(user, saveSport("러닝"), saveRegion("강남구"), "사용자");
        entityManager.flush();
        entityManager.clear();

        Profile profile = profileRepository.findWithAssociationsByUserId(user.getId()).orElseThrow();

        assertThat(Hibernate.isInitialized(profile.getSport())).isTrue();
        assertThat(Hibernate.isInitialized(profile.getRegion())).isTrue();
        assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(profile.getUser())).isTrue();
    }

    @Test
    void recommendationExcludesCurrentUser() {
        Sport sport = saveSport("러닝");
        Region region = saveRegion("강남구");
        User current = saveUser("current");
        User candidate = saveUser("candidate");
        saveProfile(current, sport, region, "현재");
        saveProfile(candidate, sport, region, "후보");

        List<Profile> result = recommendations(current.getId(), List.of(), List.of());

        assertThat(result).extracting(profile -> profile.getUser().getId())
                .containsExactly(candidate.getId());
    }

    @Test
    void recommendationAppliesMultipleSportAndRegionFilters() {
        Sport running = saveSport("러닝");
        Sport soccer = saveSport("축구");
        Sport tennis = saveSport("테니스");
        Region gangnam = saveRegion("강남구");
        Region mapo = saveRegion("마포구");
        Region songpa = saveRegion("송파구");
        User current = saveUser("current");
        User runningGangnam = saveUser("runningGangnam");
        User soccerMapo = saveUser("soccerMapo");
        User tennisGangnam = saveUser("tennisGangnam");
        User runningSongpa = saveUser("runningSongpa");
        saveProfile(current, running, gangnam, "현재");
        saveProfile(runningGangnam, running, gangnam, "러닝 강남");
        saveProfile(soccerMapo, soccer, mapo, "축구 마포");
        saveProfile(tennisGangnam, tennis, gangnam, "테니스 강남");
        saveProfile(runningSongpa, running, songpa, "러닝 송파");

        List<Profile> result = recommendations(
                current.getId(),
                List.of(running.getId(), soccer.getId()),
                List.of(gangnam.getId(), mapo.getId())
        );

        assertThat(result).extracting(profile -> profile.getUser().getId())
                .containsExactlyInAnyOrder(runningGangnam.getId(), soccerMapo.getId());
    }

    @Test
    void recommendationAppliesOnlySportFilter() {
        Sport sport = saveSport("러닝");
        Sport otherSport = saveSport("축구");
        Region region = saveRegion("강남구");
        User current = saveUser("current");
        User matching = saveUser("matching");
        User excluded = saveUser("excluded");
        saveProfile(current, sport, region, "현재");
        saveProfile(matching, sport, region, "일치");
        saveProfile(excluded, otherSport, region, "제외");

        assertThat(recommendations(current.getId(), List.of(sport.getId()), List.of()))
                .extracting(profile -> profile.getUser().getId())
                .containsExactly(matching.getId());
    }

    @Test
    void recommendationExcludesFriends() {
        Sport sport = saveSport("러닝");
        Region region = saveRegion("강남구");
        User current = saveUser("current");
        User friend = saveUser("friend");
        User candidate = saveUser("candidate");
        saveProfile(current, sport, region, "현재");
        saveProfile(friend, sport, region, "친구");
        saveProfile(candidate, sport, region, "후보");
        friendshipRepository.saveAndFlush(Friendship.create(current, friend));

        assertThat(recommendations(current.getId(), List.of(), List.of()))
                .extracting(profile -> profile.getUser().getId())
                .containsExactly(candidate.getId());
    }

    private List<Profile> recommendations(
            Long userId,
            List<Long> sportIds,
            List<Long> regionIds
    ) {
        return profileRepository.findRecommendationCandidates(
                userId,
                !sportIds.isEmpty(),
                sportIds,
                !regionIds.isEmpty(),
                regionIds
        );
    }

    private User saveUser(String loginId) {
        return userRepository.saveAndFlush(User.create(loginId, "encoded-password"));
    }

    private Sport saveSport(String name) {
        Sport sport = BeanUtils.instantiateClass(Sport.class);
        ReflectionTestUtils.setField(sport, "name", name);
        return sportRepository.saveAndFlush(sport);
    }

    private Region saveRegion(String name) {
        Region region = BeanUtils.instantiateClass(Region.class);
        ReflectionTestUtils.setField(region, "name", name);
        return regionRepository.saveAndFlush(region);
    }

    private Profile saveProfile(User user, Sport sport, Region region, String name) {
        return profileRepository.saveAndFlush(Profile.create(
                user, name, 25, Gender.MALE, sport,
                ExerciseLevel.INTERMEDIATE, region));
    }
}
