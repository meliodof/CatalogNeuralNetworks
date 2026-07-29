package project.DTO;

public class NeuronetNameDto {

    private final String name;
    private final long reviewCount;

    public NeuronetNameDto(String name, long reviewCount) {
        this.name = name;
        this.reviewCount = reviewCount;
    }

    public String getName() { return name; }
    public long getReviewCount() { return reviewCount; }
}
