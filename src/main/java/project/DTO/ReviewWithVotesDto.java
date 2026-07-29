package project.DTO;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewWithVotesDto {

    private final Long idReview;
    private final String username;
    private final Integer rating;
    private final String comment;
    private final LocalDateTime createdAt;
    private final long likes;
    private final long dislikes;
    private final long voteScore;

    public ReviewWithVotesDto(Long idReview, String username, Integer rating, String comment,
                              LocalDateTime createdAt, long likes, long dislikes, long voteScore) {
        this.idReview = idReview;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.likes = likes;
        this.dislikes = dislikes;
        this.voteScore = voteScore;
    }

    public Long getIdReview() { return idReview; }
    public String getUsername() { return username; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public long getLikes() { return likes; }
    public long getDislikes() { return dislikes; }
    public long getVoteScore() { return voteScore; }
}
