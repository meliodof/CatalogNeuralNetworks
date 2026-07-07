package project.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id_categories", nullable = false)
    private Long idCategories;

    @Column(name="name")
    private String name;

    @OneToMany(mappedBy = "category")
    private List<Neuronet> neuronets;

}
