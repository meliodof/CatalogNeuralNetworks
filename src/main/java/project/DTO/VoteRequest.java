package project.DTO;

import jakarta.validation.constraints.NotNull;

public class VoteRequest {

    @NotNull(message = "Отзыв не указан")
    private Long reviewId;

    @NotNull(message = "Пользователь не указан")
    private Long userId;

    @NotNull(message = "Голос не указан")
    private Integer vote;

    @NotNull(message = "Нейросеть не указана")
    private Long neuronetId;

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getVote() { return vote; }
    public void setVote(Integer vote) { this.vote = vote; }

    public Long getNeuronetId() { return neuronetId; }
    public void setNeuronetId(Long neuronetId) { this.neuronetId = neuronetId; }
}
