package project;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import project.DTO.CategoryGroupDto;
import project.DTO.NeuronetCardDto;
import project.DTO.ReviewWithVotesDto;
import project.Entity.Neuronet;
import project.Service.CategoryService;
import project.Service.NeuronetService;
import project.Service.ReviewService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class NeuronetController {

    private final NeuronetService neuronetService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;

    public NeuronetController(NeuronetService neuronetService,
                              CategoryService categoryService,
                              ReviewService reviewService) {
        this.neuronetService = neuronetService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
    }

    @GetMapping("/neuronets")
    public String getAll(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "false") boolean sortByRating,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean availableInRussia,
            @RequestParam(required = false) String pricing,
            HttpServletRequest request,
            Model model) {

        List<Neuronet> neuronets;

        if (search != null && !search.isBlank()) {
            neuronets = neuronetService.search(search);
        } else if (categoryId != null && availableInRussia != null) {
            neuronets = neuronetService.getByCategoryAndAvailability(categoryId, availableInRussia);
        } else if (categoryId != null) {
            neuronets = neuronetService.getByCategoryId(categoryId);
        } else if (availableInRussia != null) {
            neuronets = neuronetService.getByAvailableInRussia(availableInRussia);
        } else {
            neuronets = neuronetService.getAll();
        }

        model.addAttribute("neuronets", neuronets);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("sortByRating", sortByRating);
        model.addAttribute("search", search);
        model.addAttribute("availableInRussia", availableInRussia);
        model.addAttribute("pricing", pricing);

        // Если это HTMX-запрос — возвращаем только фрагмент <main>, а не всю страницу
        boolean isHtmx = "true".equals(request.getHeader("HX-Request"));
        if (isHtmx) {
            List<NeuronetCardDto> cardDtos = neuronets.stream()
                    .map(this::toCardDto)
                    .toList();
            Map<String, List<NeuronetCardDto>> grouped = cardDtos.stream()
                    .collect(Collectors.groupingBy(
                            n -> n.getCategoryName() != null ? n.getCategoryName() : "Без категории",
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));
            model.addAttribute("groupedNeuronets", grouped.entrySet().stream()
                    .map(e -> new CategoryGroupDto(e.getKey(), e.getValue()))
                    .toList());
            return "fragments/main-area :: mainArea";
        }

        return "neuronets/list";
    }

    @GetMapping("/top")
    public String getTopPopular(@RequestParam(defaultValue = "10") int limit, Model model) {
        model.addAttribute("neuronets", neuronetService.getTopPopular(limit));
        model.addAttribute("categories", categoryService.getAll());
        return "neuronets/list";
    }

    @GetMapping("/neuronets/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Neuronet neuronet = neuronetService.getById(id)
                .orElseThrow(() -> new RuntimeException("Нейросеть не найдена"));

        // Дублирование логики FIX: используем централизованный метод сервиса
        List<ReviewWithVotesDto> reviewsWithVotes = reviewService.getReviewsWithVotes(id);

        Double avgRating = reviewService.getAverageRating(id);
        Long reviewCount = reviewService.getReviewCount(id);

        model.addAttribute("neuronet", neuronet);
        model.addAttribute("reviews", reviewsWithVotes);
        model.addAttribute("avgRating", avgRating != null ? avgRating : 0.0);
        model.addAttribute("reviewCount", reviewCount != null ? reviewCount : 0);

        return "neuronet-detail";
    }

    private NeuronetCardDto toCardDto(Neuronet n) {
        List<String> tagNames = n.getTags() != null
                ? n.getTags().stream().map(tag -> tag.getName()).toList()
                : List.of();
        String categoryName = n.getCategory() != null ? n.getCategory().getName() : null;
        var ratingInfo = neuronetService.getRatingInfo(n.getIdNeuronet());
        Double avg = ratingInfo.getAverageRating();
        Long count = ratingInfo.getReviewCount();

        return new NeuronetCardDto(
                n.getIdNeuronet(), n.getName(), n.getDescriptionNetwork(),
                n.getNeuronetIcon(), n.getAvailableInRussia(),
                categoryName, tagNames, avg, count
        );
    }
}