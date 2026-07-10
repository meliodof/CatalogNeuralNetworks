package project.Repository;

import project.Entity.Neuronet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RepNeuronet extends JpaRepository<Neuronet,Long> {

    List<Neuronet> findByCategory_IdCategories(Long categoryId);

    // Пункт 6: поиск по названию
    List<Neuronet> findByNameContainingIgnoreCase(String name);

    // Пункт 6: поиск по названию ИЛИ описанию
    List<Neuronet> findByNameContainingIgnoreCaseOrDescriptionNetworkContainingIgnoreCase(
            String name, String description);

    // Пункт 5 и 7: сортировка по рейтингу и популярности
    @Query(value = """
    SELECT n.id_neuronet, n.name, n.description_network, n.extended_description,
           n.neuronet_icon, n.available_in_russia, n.created_at, n.id_categories,
           AVG(r.rating) AS avg_rating,
           CAST(COUNT(r.id_review) AS BIGINT) AS total_reviews
    FROM neuronets n
    LEFT JOIN reviews r ON n.id_neuronet = r.id_neuronet
    GROUP BY n.id_neuronet
    ORDER BY avg_rating DESC NULLS LAST, total_reviews DESC
    """, nativeQuery = true)
    List<Object[]> findAllSortedByRating();

    // Пункт 8: фильтр по доступности в России
    List<Neuronet> findByAvailableInRussia(Boolean available);

}
