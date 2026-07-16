package project.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "review_votes", uniqueConstraints = @UniqueConstraint(columnNames = {"id_review", "id_user"}))
@Getter
@Setter
public class ReviewVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_review", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer vote; // 1 = лайк, -1 = дизлайк
}