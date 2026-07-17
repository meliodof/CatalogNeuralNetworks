package project.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "neuronets")
@Getter
@Setter
public class Neuronet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // SERIAL = IDENTITY
    @Column(name = "id_neuronet", nullable = false)
    private Long idNeuronet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categories")
    private Category category;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description_network")
    private String descriptionNetwork;

    @Column(name = "extended_description")
    private String extendedDescription;

    @Column(name = "neuronet_icon")
    private String neuronetIcon;

    @Column(name = "available_in_russia")
    private Boolean availableInRussia = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Как использовать
    @Column(name = "usage_registration", columnDefinition = "TEXT")
    private String usageRegistration;

    @Column(name = "usage_setup", columnDefinition = "TEXT")
    private String usageSetup;

    @Column(name = "usage_general", columnDefinition = "TEXT")
    private String usageGeneral;

    // Ссылки
    @Column(name = "official_url", length = 500)
    private String officialUrl;

    @Column(name = "api_url", length = 500)
    private String apiUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Column(name = "pricing_info", columnDefinition = "TEXT")
    private String pricingInfo;

    // Связь с отзывами (один ко многим)
    @OneToMany(mappedBy = "neuronet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    // Связь с тегами (многие ко многим через neuronet_tags)
    @ManyToMany
    @JoinTable(
            name = "neuronet_tags",
            joinColumns = @JoinColumn(name = "id_neuronet"),
            inverseJoinColumns = @JoinColumn(name = "id_tag")
    )
    private List<Tag> tags;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}