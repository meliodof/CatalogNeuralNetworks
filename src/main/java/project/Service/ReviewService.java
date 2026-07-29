package project.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.DTO.ReviewWithVotesDto;
import project.Entity.Neuronet;
import project.Entity.Review;
import project.Entity.ReviewVote;
import project.Entity.User;
import project.Repository.RepReview;
import project.Repository.RepReviewVote;
import project.Repository.RepUser;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final RepReview repReview;
    private final RepReviewVote repReviewVote;
    private final RepUser repUser;

    public ReviewService(RepReview repReview, RepReviewVote repReviewVote, RepUser repUser){
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

    public List<Review> getByUserId(Long userId) {
        return repReview.findByUser_IdUser(userId);
    }

    // ===== Дублирование логики FIX: централизованный сбор vote stats =====

    /**
     * Получает список отзывов с подсчитанными голосами.
     * Сортирует по vote score (убывание).
     * Использует batch-запрос для оптимизации.
     */
    public List<ReviewWithVotesDto> getReviewsWithVotes(Long neuronetId) {
        List<Review> reviews = repReview.findByNeuronet_IdNeuronet(neuronetId);
        if (reviews.isEmpty()) {
            return Collections.emptyList();
        }

        // Batch-запрос для всех голосов
        List<Long> reviewIds = reviews.stream().map(Review::getIdReview).toList();
        Map<Long, Object[]> voteStatsMap = new HashMap<>();
        
        List<Object[]> batchStats = repReviewVote.getBatchVoteStats(reviewIds);
        for (Object[] row : batchStats) {
            voteStatsMap.put(((Number) row[0]).longValue(), row);
        }

        List<ReviewWithVotesDto> result = reviews.stream()
                .map(review -> {
                    Long rid = review.getIdReview();
                    Object[] stats = voteStatsMap.get(rid);
                    
                    long likes = stats != null ? ((Number) stats[1]).longValue() : 0;
                    long dislikes = stats != null ? ((Number) stats[2]).longValue() : 0;
                    long voteScore = stats != null ? ((Number) stats[3]).longValue() : 0;

                    return new ReviewWithVotesDto(
                            rid,
                            review.getUser() != null ? review.getUser().getUsername() : "Аноним",
                            review.getRating(),
                            review.getComment(),
                            review.getCreatedAt(),
                            likes,
                            dislikes,
                            voteScore
                    );
                })
                .sorted(Comparator.comparingLong(ReviewWithVotesDto::getVoteScore).reversed())
                .collect(Collectors.toList());

        return result;
    }

    /**
     * Получает vote stats map для существующих Review-объектов.
     * Используется в NeuronetController для передачи в шаблон.
     */
    public Map<Long, ReviewWithVotesDto> getReviewVotesMap(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> reviewIds = reviews.stream().map(Review::getIdReview).toList();
        Map<Long, Object[]> voteStatsMap = new HashMap<>();
        
        List<Object[]> batchStats = repReviewVote.getBatchVoteStats(reviewIds);
        for (Object[] row : batchStats) {
            voteStatsMap.put(((Number) row[0]).longValue(), row);
        }

        Map<Long, ReviewWithVotesDto> result = new LinkedHashMap<>();
        for (Review review : reviews) {
            Long rid = review.getIdReview();
            Object[] stats = voteStatsMap.get(rid);
            
            long likes = stats != null ? ((Number) stats[1]).longValue() : 0;
            long dislikes = stats != null ? ((Number) stats[2]).longValue() : 0;
            long voteScore = stats != null ? ((Number) stats[3]).longValue() : 0;

            result.put(rid, new ReviewWithVotesDto(
                    rid,
                    review.getUser() != null ? review.getUser().getUsername() : "Аноним",
                    review.getRating(),
                    review.getComment(),
                    review.getCreatedAt(),
                    likes,
                    dislikes,
                    voteScore
            ));
        }

        return result;
    }
}
