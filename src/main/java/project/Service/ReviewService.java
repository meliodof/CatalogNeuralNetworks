package project.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.Entity.Neuronet;
import project.Entity.Review;
import project.Entity.User;
import project.Repository.RepReview;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final RepReview review;

    private final RepReview repReview;

    public ReviewService (RepReview review,
                          RepReview repReview){
        this.review=review;
        this.repReview = repReview;
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

    // Количество отзывов
    public Long getReviewCount(Long neuronetId) {
        return repReview.countByNeuronet_IdNeuronet(neuronetId);
    }

    // Проверка: оставлял ли пользователь отзыв
    public boolean hasUserReviewed(Long neuronetId, Long userId) {
        return repReview.existsByNeuronet_IdNeuronetAndUser_IdUser(neuronetId, userId);
    }

    @Transactional
    public void delete(Long reviewId) {
        repReview.deleteById(reviewId);
    }
}
