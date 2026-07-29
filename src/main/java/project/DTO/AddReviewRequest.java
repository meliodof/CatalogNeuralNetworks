package project.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AddReviewRequest {

    @NotNull(message = "Нейросеть не указана")
    private Long neuronetId;

    @NotNull(message = "Рейтинг обязателен")
    @Min(value = 1, message = "Рейтинг: от 1 до 5")
    @Max(value = 5, message = "Рейтинг: от 1 до 5")
    private Integer rating;

    private String comment;

    public Long getNeuronetId() { return neuronetId; }
    public void setNeuronetId(Long neuronetId) { this.neuronetId = neuronetId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
