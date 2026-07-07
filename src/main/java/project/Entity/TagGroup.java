package project.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "tag_groups")
@Getter
@Setter
public class TagGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tag_group", nullable = false)
    private Long idTagGroup;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @OneToMany(mappedBy = "tagGroup")
    private List<Tag> tags;
}