package project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.Entity.ReviewVote;
import java.util.Optional;

public interface RepReviewVote extends JpaRepository<ReviewVote, Long> {

    Optional<ReviewVote> findByReview_IdReviewAndUser_IdUser(Long reviewId, Long userId);

    @Query("SELECT COALESCE(SUM(rv.vote), 0) FROM ReviewVote rv WHERE rv.review.idReview = :reviewId")
    Long getScoreByReviewId(@Param("reviewId") Long reviewId);

    boolean existsByReview_IdReviewAndUser_IdUser(Long reviewId, Long userId);
}