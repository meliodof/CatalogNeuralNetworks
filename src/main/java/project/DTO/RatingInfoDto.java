package project.DTO;

public class RatingInfoDto {

    private final double averageRating;
    private final long reviewCount;

    public RatingInfoDto(Double averageRating, Long reviewCount) {
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.reviewCount = reviewCount != null ? reviewCount : 0L;
    }

    public double getAverageRating() { return averageRating; }
    public long getReviewCount() { return reviewCount; }
}
