package project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.Entity.Tag;

import java.util.List;
import java.util.Optional;

public interface RepTag extends JpaRepository<Tag,Long> {

    // Поиск тега по имени
    Optional<Tag> findByName(String name);

    // Поиск тегов по группе тегов
    @Query("SELECT t FROM Tag t JOIN FETCH t.tagGroup WHERE t.tagGroup.name = :groupName")
    List<Tag> findByTagGroupName(@Param("groupName") String groupName);

    // Поиск тегов группы по slug
    @Query("SELECT t FROM Tag t JOIN FETCH t.tagGroup tg WHERE tg.slug = :slug")
    List<Tag> findByTagGroupSlug(@Param("slug") String slug);
}
