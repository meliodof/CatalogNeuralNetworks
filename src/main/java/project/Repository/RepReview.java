package project.Repository;

import project.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RepReview extends JpaRepository<Review,Long> {

    // Отзывы конкретной нейросети
    List<Review> findByNeuronet_IdNeuronet(Long neuronetId);

    // Проверка: оставлял ли пользователь отзыв
    boolean existsByNeuronet_IdNeuronetAndUser_IdUser(Long neuronetId, Long userId);

    // Средний рейтинг нейросети
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.neuronet.idNeuronet = :neuronetId")
    Double getAverageRatingByNeuronetId(@Param("neuronetId") Long neuronetId);

    // Количество отзывов нейросети
    Long countByNeuronet_IdNeuronet(Long neuronetId);
}
