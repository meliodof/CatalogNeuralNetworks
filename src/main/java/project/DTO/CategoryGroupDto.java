package project.DTO;

import java.util.List;

public class CategoryGroupDto {

    private final String categoryName;
    private final List<NeuronetCardDto> neuronets;

    public CategoryGroupDto(String categoryName, List<NeuronetCardDto> neuronets) {
        this.categoryName = categoryName;
        this.neuronets = neuronets;
    }

    public String getCategoryName() { return categoryName; }
    public List<NeuronetCardDto> getNeuronets() { return neuronets; }
}
