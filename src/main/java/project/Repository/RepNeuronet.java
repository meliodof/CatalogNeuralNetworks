package project.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.Entity.Neuronet;

import java.util.List;

public interface RepNeuronet extends JpaRepository<Neuronet,Long> {

    // N+1 FIX: JOIN FETCH для категории и тегов
    @EntityGraph(attributePaths = {"category", "tags"})
    @Query("SELECT n FROM Neuronet n LEFT JOIN FETCH n.category LEFT JOIN FETCH n.tags")
    List<Neuronet> findAllWithCategoryAndTags();

    // N+1 FIX: пагинация с JOIN FETCH
    @EntityGraph(attributePaths = {"category", "tags"})
    @Query("SELECT n FROM Neuronet n LEFT JOIN FETCH n.category LEFT JOIN FETCH n.tags")
    Page<Neuronet> findAllWithCategoryAndTags(Pageable pageable);

    // N+1 FIX: пагинация с фильтрами (без тегов — теги фильтруются в сервисе)
    @EntityGraph(attributePaths = {"category", "tags"})
    @Query("SELECT n FROM Neuronet n LEFT JOIN FETCH n.category LEFT JOIN FETCH n.tags " +
           "WHERE (:categoryId IS NULL OR n.category.idCategories = :categoryId) " +
           "AND (:availableInRussia IS NULL OR n.availableInRussia = :availableInRussia)")
    Page<Neuronet> findAllWithFilters(@Param("categoryId") Long categoryId,
                                       @Param("availableInRussia") Boolean availableInRussia,
                                       Pageable pageable);

    // N+1 FIX: поиск с JOIN FETCH
    @EntityGraph(attributePaths = {"category", "tags"})
    @Query("SELECT n FROM Neuronet n LEFT JOIN FETCH n.category LEFT JOIN FETCH n.tags " +
           "WHERE LOWER(n.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(n.descriptionNetwork) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Neuronet> searchWithCategoryAndTags(@Param("query") String query);

    // N+1 FIX: фильтрация по категории
    @EntityGraph(attributePaths = {"tags"})
    @Query("SELECT n FROM Neuronet n LEFT JOIN FETCH n.tags WHERE n.category.idCategories = :categoryId")
    List<Neuronet> findByCategory_IdCategoriesWithTags(@Param("categoryId") Long categoryId);

    // N+1 FIX: фильтрация по доступности
    @EntityGraph(attributePaths = {"category", "tags"})
    @Query("SELECT n FROM Neuronet n LEFT JOIN FETCH n.category LEFT JOIN FETCH n.tags " +
           "WHERE n.availableInRussia = :available")
    List<Neuronet> findByAvailableInRussiaWithAll(@Param("available") Boolean available);

    // N+1 FIX: пагинация по категории
    @EntityGraph(attributePaths = {"tags"})
    @Query("SELECT n FROM Neuronet n LEFT JOIN FETCH n.tags WHERE n.category.idCategories = :categoryId")
    Page<Neuronet> findByCategory_IdCategoriesWithTags(@Param("categoryId") Long categoryId, Pageable pageable);

    // N+1 FIX: пагинация по доступности
    @EntityGraph(attributePaths = {"category", "tags"})
    @Query("SELECT n FROM Neuronet n LEFT JOIN FETCH n.category LEFT JOIN FETCH n.tags " +
           "WHERE n.availableInRussia = :available")
    Page<Neuronet> findByAvailableInRussiaWithAll(@Param("available") Boolean available, Pageable pageable);

    // Поиск (без JOIN FETCH — для поиска используем searchWithCategoryAndTags)
    List<Neuronet> findByCategory_IdCategories(Long categoryId);
    List<Neuronet> findByNameContainingIgnoreCase(String name);
    List<Neuronet> findByNameContainingIgnoreCaseOrDescriptionNetworkContainingIgnoreCase(
            String name, String description);
    List<Neuronet> findByAvailableInRussia(Boolean available);

    // Сортировка по рейтингу — агрегированный запрос
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

    // Пагинация по рейтингу
    @Query(value = """
    SELECT n.* FROM neuronets n
    LEFT JOIN reviews r ON n.id_neuronet = r.id_neuronet
    GROUP BY n.id_neuronet
    ORDER BY COALESCE(AVG(r.rating), 0) DESC NULLS LAST, COUNT(r.id_review) DESC
    LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Neuronet> findTopPopular(@Param("limit") int limit, @Param("offset") int offset);

    // Топ популярных с пагинацией (Page API)
    @Query("SELECT n FROM Neuronet n")
    Page<Neuronet> findAllPaged(Pageable pageable);

    // Количество нейросетей
    long count();

    // Фильтр по тегу (через tag name)
    @Query("SELECT n FROM Neuronet n JOIN n.tags t WHERE t.name = :tagName")
    List<Neuronet> findByTagName(@Param("tagName") String tagName);

    // Комбинированный фильтр по категории и доступности
    @Query("SELECT n FROM Neuronet n WHERE n.category.idCategories = :categoryId AND n.availableInRussia = :available")
    List<Neuronet> findByCategoryAndAvailability(@Param("categoryId") Long categoryId,
                                                  @Param("available") Boolean available);
}
