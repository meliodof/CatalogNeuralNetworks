package project.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.Entity.Neuronet;
import project.Entity.Review;
import project.Entity.ReviewVote;
import project.Entity.User;
import project.Repository.RepReview;
import project.Repository.RepReviewVote;
import project.Repository.RepUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReviewService { ;

    private final RepReview repReview;
    private final RepReviewVote repReviewVote;
    private final RepUser repUser;

    public ReviewService (RepReview review,
                          RepReview repReview, RepReviewVote repReviewVote, RepUser repUser){
        this.repReview = repReview;
        this.repReviewVote = repReviewVote;
        this.repUser = repUser;
    }

    // Пункт 4: Поставить оценку (с комментарием или без)
    @Transactional
    public Review addReview(Neuronet neuronet, User user, int rating, String comment) {
        // Проверка: не ставил ли уже
        if (repReview.existsByNeuronet_IdNeuronetAndUser_IdUser(
                neuronet.getIdNeuronet(), user.getIdUser())) {
            throw new IllegalStateException("Пользователь уже оставил отзыв на эту нейросеть");
        }

        Review review = new Review();
        review.setNeuronet(neuronet);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);  // может быть null
        review.setCreatedAt(LocalDateTime.now());

        return repReview.save(review);
    }

    // Все отзывы нейросети
    public List<Review> getByNeuronetId(Long neuronetId) {
        return repReview.findByNeuronet_IdNeuronet(neuronetId);
    }

    // Средний рейтинг нейросети
    public Double getAverageRating(Long neuronetId) {
        return repReview.getAverageRatingByNeuronetId(neuronetId);
    }

    public Long getReviewCount(Long neuronetId) {
        return repReview.countByNeuronet_IdNeuronet(neuronetId);
    }


    public boolean hasUserReviewed(Long neuronetId, Long userId) {
        return repReview.existsByNeuronet_IdNeuronetAndUser_IdUser(neuronetId, userId);
    }

    @Transactional
    public void delete(Long reviewId) {
        repReview.deleteById(reviewId);
    }

    @Transactional
    public void vote(Long reviewId, Long userId, int vote) {
        Review review = repReview.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));
        User user = repUser.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Optional<ReviewVote> existing = repReviewVote.findByReview_IdReviewAndUser_IdUser(reviewId, userId);
        if (existing.isPresent()) {
            if (existing.get().getVote() == vote) {
                repReviewVote.delete(existing.get()); // отмена голоса
            } else {
                existing.get().setVote(vote);
                repReviewVote.save(existing.get());
            }
        } else {
            ReviewVote rv = new ReviewVote();
            rv.setReview(review);
            rv.setUser(user);
            rv.setVote(vote);
            repReviewVote.save(rv);
        }
    }

    public Long getVoteScore(Long reviewId) {
        return repReviewVote.getScoreByReviewId(reviewId);
    }

    public boolean hasUserVoted(Long reviewId, Long userId) {
        return repReviewVote.existsByReview_IdReviewAndUser_IdUser(reviewId, userId);
    }

    public Long countLikes(Long reviewId) {
        return repReviewVote.countLikesByReviewId(reviewId);
    }

    public Long countDislikes(Long reviewId) {
        return repReviewVote.countDislikesByReviewId(reviewId);
    }
}
