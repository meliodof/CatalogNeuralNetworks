package project.DTO;

import java.util.List;

/**
 * Данные одной нейросети для страницы сравнения (/neuronets/compare).
 * Теги разложены по группам, чтобы шаблон строил таблицу построчно
 * (одна строка сравнения = одна группа параметров).
 */
public class NeuronetCompareDto {

    private final Long idNeuronet;
    private final String name;
    private final String neuronetIcon;
    private final String categoryName;
    private final Boolean availableInRussia;
    private final List<String> pricingTags;   // группа тегов "Цена"
    private final List<String> featureTags;   // группа тегов "Особенности"
    private final double averageRating;
    private final long reviewCount;
    private final String pricingInfo;
    private final String officialUrl;
    private final String apiUrl;
    private final String githubUrl;

    public NeuronetCompareDto(Long idNeuronet, String name, String neuronetIcon,
                               String categoryName, Boolean availableInRussia,
                               List<String> pricingTags, List<String> featureTags,
                               Double averageRating, Long reviewCount,
                               String pricingInfo, String officialUrl,
                               String apiUrl, String githubUrl) {
        this.idNeuronet = idNeuronet;
        this.name = name;
        this.neuronetIcon = neuronetIcon;
        this.categoryName = categoryName;
        this.availableInRussia = availableInRussia;
        this.pricingTags = pricingTags;
        this.featureTags = featureTags;
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.reviewCount = reviewCount != null ? reviewCount : 0L;
        this.pricingInfo = pricingInfo;
        this.officialUrl = officialUrl;
        this.apiUrl = apiUrl;
        this.githubUrl = githubUrl;
    }

    public Long getIdNeuronet() { return idNeuronet; }
    public String getName() { return name; }
    public String getNeuronetIcon() { return neuronetIcon; }
    public String getCategoryName() { return categoryName; }
    public Boolean getAvailableInRussia() { return availableInRussia; }
    public List<String> getPricingTags() { return pricingTags; }
    public List<String> getFeatureTags() { return featureTags; }
    public double getAverageRating() { return averageRating; }
    public long getReviewCount() { return reviewCount; }
    public String getPricingInfo() { return pricingInfo; }
    public String getOfficialUrl() { return officialUrl; }
    public String getApiUrl() { return apiUrl; }
    public String getGithubUrl() { return githubUrl; }
}
