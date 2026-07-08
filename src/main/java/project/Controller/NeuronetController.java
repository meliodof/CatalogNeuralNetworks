package project.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import project.Entity.Neuronet;
import project.Entity.Review;
import project.Service.CategoryService;
import project.Service.NeuronetService;
import project.Service.ReviewService;

import java.util.List;
import java.util.Optional;

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

    // Пункт 1: Весь список
    // Пункт 2: По категории
    // Пункт 5: Сортировка по рейтингу
    // Пункт 6: Поиск
    // Пункт 8: Доступность в РФ
    @GetMapping
    public String getAll(@RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false, defaultValue = "false") boolean sortByRating,
                         @RequestParam(required = false) String search,
                         @RequestParam(required = false) Boolean availableInRussia,
                         Model model) {

        List<Neuronet> neuronets;

        if (search != null && !search.isEmpty()) {
            // Пункт 6: поиск
            neuronets = neuronetService.search(search);
        } else if (categoryId != null && availableInRussia != null) {
            // Комбинированный фильтр
            neuronets = neuronetService.getByCategoryAndAvailability(categoryId, availableInRussia);
        } else if (categoryId != null) {
            // Пункт 2: по категории
            neuronets = neuronetService.getByCategoryId(categoryId);
        } else if (availableInRussia != null) {
            // Пункт 8: доступность в РФ
            neuronets = neuronetService.getByAvailableInRussia(availableInRussia);
        } else if (sortByRating) {
            // Пункт 5: сортировка по рейтингу
            neuronets = neuronetService.getAllSortedByRating();
        } else {
            // Пункт 1: всё
            neuronets = neuronetService.getAll();
        }

        model.addAttribute("neuronets", neuronets);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("sortByRating", sortByRating);
        model.addAttribute("search", search);
        model.addAttribute("availableInRussia", availableInRussia);

        return "neuronets/list";
    }

    // Пункт 3: Карточка нейросети
    @GetMapping("/{id}")
    public String getById(@PathVariable Long id, Model model) {
        Optional<Neuronet> neuronet = neuronetService.getById(id);
        if (neuronet.isEmpty()) {
            return "redirect:/neuronets";
        }

        Neuronet n = neuronet.get();
        List<Review> reviews = reviewService.getByNeuronetId(id);
        Double avgRating = reviewService.getAverageRating(id);
        Long reviewCount = reviewService.getReviewCount(id);

        model.addAttribute("neuronet", n);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating != null ? avgRating : 0.0);
        model.addAttribute("reviewCount", reviewCount);

        return "neuronets/detail";
    }

    // Пункт 7: Топ популярных
    @GetMapping("/top")
    public String getTopPopular(@RequestParam(defaultValue = "10") int limit, Model model) {
        model.addAttribute("neuronets", neuronetService.getTopPopular(limit));
        model.addAttribute("categories", categoryService.getAll());
        return "neuronets/list";
    }
}
