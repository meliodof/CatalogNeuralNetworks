package project.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "tags")
@Getter
@Setter
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tag", nullable = false)
    private Long idTag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tag_group", nullable = false)
    private TagGroup tagGroup;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    // Обратная связь
    @ManyToMany(mappedBy = "tags")
    private List<Neuronet> neuronets;
}