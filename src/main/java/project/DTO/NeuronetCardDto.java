package project.DTO;

import java.util.List;

public class NeuronetCardDto {

    private final Long idNeuronet;
    private final String name;
    private final String descriptionNetwork;
    private final String neuronetIcon;
    private final Boolean availableInRussia;
    private final String categoryName;
    private final List<String> tagNames;
    private final double averageRating;
    private final long reviewCount;

    public NeuronetCardDto(Long idNeuronet, String name, String descriptionNetwork,
                           String neuronetIcon, Boolean availableInRussia,
                           String categoryName, List<String> tagNames,
                           Double averageRating, Long reviewCount) {
        this.idNeuronet = idNeuronet;
        this.name = name;
        this.descriptionNetwork = descriptionNetwork;
        this.neuronetIcon = neuronetIcon;
        this.availableInRussia = availableInRussia;
        this.categoryName = categoryName;
        this.tagNames = tagNames;
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.reviewCount = reviewCount != null ? reviewCount : 0L;
    }

    public Long getIdNeuronet() { return idNeuronet; }
    public String getName() { return name; }
    public String getDescriptionNetwork() { return descriptionNetwork; }
    public String getNeuronetIcon() { return neuronetIcon; }
    public Boolean getAvailableInRussia() { return availableInRussia; }
    public String getCategoryName() { return categoryName; }
    public List<String> getTagNames() { return tagNames; }
    public double getAverageRating() { return averageRating; }
    public long getReviewCount() { return reviewCount; }
}
