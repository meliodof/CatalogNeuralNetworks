package project.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.Entity.Review;

import java.util.List;

public interface RepReview extends JpaRepository<Review,Long> {

    // Отзывы конкретной нейросети
    List<Review> findByNeuronet_IdNeuronet(Long neuronetId);

    // Пагинация отзывов нейросети
    Page<Review> findByNeuronet_IdNeuronet(Long neuronetId, Pageable pageable);

    // Проверка: оставлял ли пользователь отзыв
    boolean existsByNeuronet_IdNeuronetAndUser_IdUser(Long neuronetId, Long userId);

    // Средний рейтинг нейросети
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.neuronet.idNeuronet = :neuronetId")
    Double getAverageRatingByNeuronetId(@Param("neuronetId") Long neuronetId);

    // Количество отзывов нейросети
    Long countByNeuronet_IdNeuronet(Long neuronetId);

    List<Review> findByUser_IdUser(Long userId);

    // Количество отзывов по нейросети (для агрегации)
    long countByNeuronet_IdNeuronetAndRating(Long neuronetId, Integer rating);
}
