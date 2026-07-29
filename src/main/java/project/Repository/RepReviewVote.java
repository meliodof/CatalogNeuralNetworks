package project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.Entity.ReviewVote;

import java.util.List;
import java.util.Optional;

public interface RepReviewVote extends JpaRepository<ReviewVote, Long> {

    Optional<ReviewVote> findByReview_IdReviewAndUser_IdUser(Long reviewId, Long userId);

    @Query("SELECT COALESCE(SUM(rv.vote), 0) FROM ReviewVote rv WHERE rv.review.idReview = :reviewId")
    Long getScoreByReviewId(@Param("reviewId") Long reviewId);

    boolean existsByReview_IdReviewAndUser_IdUser(Long reviewId, Long userId);

    @Query("SELECT COUNT(rv) FROM ReviewVote rv WHERE rv.review.idReview = :reviewId AND rv.vote = 1")
    Long countLikesByReviewId(@Param("reviewId") Long reviewId);

    @Query("SELECT COUNT(rv) FROM ReviewVote rv WHERE rv.review.idReview = :reviewId AND rv.vote = -1")
    Long countDislikesByReviewId(@Param("reviewId") Long reviewId);

    // Оптимизация:批量获取多个 review 的投票统计
    @Query("SELECT rv.review.idReview, " +
           "COALESCE(SUM(CASE WHEN rv.vote = 1 THEN 1 ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN rv.vote = -1 THEN 1 ELSE 0 END), 0), " +
           "COALESCE(SUM(rv.vote), 0) " +
           "FROM ReviewVote rv " +
           "WHERE rv.review.idReview IN :reviewIds " +
           "GROUP BY rv.review.idReview")
    List<Object[]> getBatchVoteStats(@Param("reviewIds") List<Long> reviewIds);
}