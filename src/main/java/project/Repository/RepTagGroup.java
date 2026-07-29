package project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.Entity.TagGroup;

import java.util.Optional;

public interface RepTagGroup extends JpaRepository<TagGroup,Long> {
    Optional<TagGroup> findBySlug(String slug);
    Optional<TagGroup> findByName(String name);
}
