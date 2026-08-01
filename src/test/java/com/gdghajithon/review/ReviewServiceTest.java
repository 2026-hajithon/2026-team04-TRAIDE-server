package com.gdghajithon.review;

import com.gdghajithon.appointment.Appointment;
import com.gdghajithon.appointment.AppointmentQueryService;
import com.gdghajithon.appointment.AppointmentRepository;
import com.gdghajithon.friend.FriendService;
import com.gdghajithon.friend.Friendship;
import com.gdghajithon.friend.FriendshipRepository;
import com.gdghajithon.global.exception.BusinessException;
import com.gdghajithon.global.exception.ErrorCode;
import com.gdghajithon.image.ImageUrlResolver;
import com.gdghajithon.profile.ExerciseLevel;
import com.gdghajithon.profile.Gender;
import com.gdghajithon.profile.Profile;
import com.gdghajithon.profile.ProfileRepository;
import com.gdghajithon.region.Region;
import com.gdghajithon.region.RegionRepository;
import com.gdghajithon.review.dto.ReviewCreateRequest;
import com.gdghajithon.sport.Sport;
import com.gdghajithon.sport.SportRepository;
import com.gdghajithon.user.User;
import com.gdghajithon.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        ReviewService.class,
        FriendService.class,
        AppointmentQueryService.class,
        ImageUrlResolver.class
})
class ReviewServiceTest {

    @Autowired private ReviewService reviewService;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private SportRepository sportRepository;
    @Autowired private RegionRepository regionRepository;

    private User writer;
    private User receiver;

    @BeforeEach
    void setUp() {
        writer = saveUser("writer");
        receiver = saveUser("receiver");
        saveProfile(writer, "작성자");
    }

    @Test
    void friendWithAppointmentCanWriteReview() {
        makeEligible();

        var response = reviewService.create(
                writer.getId(),
                receiver.getId(),
                new ReviewCreateRequest(5, "즐거웠어요", "https://example.com/review.jpg")
        );

        assertThat(response.id()).isNotNull();
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.content()).isEqualTo("즐거웠어요");
        assertThat(response.imageUrl()).isEqualTo("https://example.com/review.jpg");
    }

    @Test
    void sameFriendCanReceiveMultipleReviews() {
        makeEligible();

        reviewService.create(writer.getId(), receiver.getId(), request(4));
        reviewService.create(writer.getId(), receiver.getId(), request(5));

        assertThat(reviewRepository.countByReceiverId(receiver.getId())).isEqualTo(2);
        assertThat(reviewRepository.findAverageRatingByReceiverId(receiver.getId()))
                .isEqualTo(4.5);
    }

    @Test
    void nonFriendCannotWriteReview() {
        saveAppointment();

        assertError(
                () -> reviewService.create(writer.getId(), receiver.getId(), request(5)),
                ErrorCode.NOT_FRIEND
        );
    }

    @Test
    void friendWithoutAppointmentCannotWriteReview() {
        friendshipRepository.saveAndFlush(Friendship.create(writer, receiver));

        assertError(
                () -> reviewService.create(writer.getId(), receiver.getId(), request(5)),
                ErrorCode.APPOINTMENT_REQUIRED
        );
    }

    @Test
    void ratingMustBeBetweenOneAndFive() {
        makeEligible();

        assertError(
                () -> reviewService.create(writer.getId(), receiver.getId(), request(0)),
                ErrorCode.INVALID_RATING
        );
        assertError(
                () -> reviewService.create(writer.getId(), receiver.getId(), request(6)),
                ErrorCode.INVALID_RATING
        );
        assertError(
                () -> reviewService.create(writer.getId(), receiver.getId(), request(null)),
                ErrorCode.INVALID_RATING
        );
    }

    @Test
    void anyoneCanViewReceivedReviewsInLatestOrder() {
        Review oldReview = reviewRepository.saveAndFlush(
                Review.create(writer, receiver, 4, "먼저 작성", null));
        ReflectionTestUtils.setField(oldReview, "createdAt", LocalDateTime.now().minusDays(1));
        Review latestReview = reviewRepository.saveAndFlush(
                Review.create(writer, receiver, 5, "나중 작성", null));

        var responses = reviewService.getReviews(receiver.getId());

        assertThat(responses).extracting("id")
                .containsExactly(latestReview.getId(), oldReview.getId());
        assertThat(responses.get(0).writer().id()).isEqualTo(writer.getId());
        assertThat(responses.get(0).writer().name()).isEqualTo("작성자");
        assertThat(responses.get(0).writer().imageUrl())
                .isEqualTo("/images/sports/tennis.png");
    }

    private ReviewCreateRequest request(Integer rating) {
        return new ReviewCreateRequest(rating, null, null);
    }

    private void makeEligible() {
        friendshipRepository.saveAndFlush(Friendship.create(writer, receiver));
        saveAppointment();
    }

    private void saveAppointment() {
        appointmentRepository.saveAndFlush(Appointment.create(
                writer,
                receiver,
                writer,
                LocalDateTime.now().plusDays(1),
                null
        ));
    }

    private User saveUser(String loginId) {
        return userRepository.saveAndFlush(User.create(loginId, "encoded-password"));
    }

    private void saveProfile(User user, String name) {
        Sport sport = BeanUtils.instantiateClass(Sport.class);
        ReflectionTestUtils.setField(sport, "name", "테니스");
        ReflectionTestUtils.setField(sport, "imageUrl", "/images/sports/tennis.png");
        sport = sportRepository.saveAndFlush(sport);
        Region region = BeanUtils.instantiateClass(Region.class);
        ReflectionTestUtils.setField(region, "name", "양천구");
        region = regionRepository.saveAndFlush(region);
        profileRepository.saveAndFlush(Profile.create(
                user,
                name,
                25,
                Gender.MALE,
                sport,
                ExerciseLevel.INTERMEDIATE,
                region
        ));
    }

    private void assertError(
            org.assertj.core.api.ThrowableAssert.ThrowingCallable action,
            ErrorCode errorCode
    ) {
        assertThatThrownBy(action)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
