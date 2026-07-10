package project;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import project.Entity.Category;
import project.Entity.Neuronet;
import project.Service.CategoryService;
import project.Service.NeuronetService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final NeuronetService neuronetService;
    private final CategoryService categoryService;

    public HomeController(NeuronetService neuronetService, CategoryService categoryService) {
        this.neuronetService = neuronetService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) Boolean availableInRussia,
                       @RequestParam(required = false, defaultValue = "false") boolean sortByRating,
                       @RequestParam(required = false) String pricing,
                       Model model,
                       HttpServletRequest request) {

        List<Neuronet> filtered = getFilteredNeuronets(categoryId, search, availableInRussia, sortByRating, pricing);

        Map<String, List<Neuronet>> grouped = new LinkedHashMap<>();
        if (categoryId != null) {
            Category cat = categoryService.getById(categoryId).orElse(null);
            if (cat != null) {
                grouped.put(cat.getName(), filtered);
            }
        } else if (search != null && !search.isBlank()) {
            grouped.put("Результаты поиска: «" + search + "»", filtered);
        } else {
            Map<Category, List<Neuronet>> byCategory = filtered.stream()
                    .collect(Collectors.groupingBy(Neuronet::getCategory, LinkedHashMap::new, Collectors.toList()));
            byCategory.forEach((cat, list) -> grouped.put(cat.getName(), list));
        }

        model.addAttribute("groupedNeuronets", grouped);
        model.addAttribute("topNeuronets", neuronetService.getTopPopular(5));
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("search", search);
        model.addAttribute("availableInRussia", availableInRussia);
        model.addAttribute("sortByRating", sortByRating);
        model.addAttribute("pricing", pricing);

        if ("true".equals(request.getHeader("HX-Request"))) {
            return "fragments/main-area :: mainArea";
        }
        return "index";
    }

    private List<Neuronet> getFilteredNeuronets(Long categoryId, String search,
                                                Boolean availableInRussia, boolean sortByRating,
                                                String pricing) {
        List<Neuronet> filtered;

        // 1. Базовая выборка
        if (search != null && !search.isBlank()) {
            filtered = neuronetService.search(search);
        } else if (categoryId != null) {
            filtered = neuronetService.getByCategoryId(categoryId);
        } else {
            filtered = neuronetService.getAll();
        }

        // 2. Фильтр по доступности в РФ
        if (availableInRussia != null) {
            filtered = filtered.stream()
                    .filter(n -> n.getAvailableInRussia() == availableInRussia)
                    .collect(Collectors.toList());
        }

        // 3. Фильтр по цене (Бесплатно / Платно)
        if (pricing != null) {
            if ("free".equals(pricing)) {
                filtered = filtered.stream()
                        .filter(n -> n.getTags() != null && n.getTags().stream()
                                .anyMatch(t -> t.getName().equals("Бесплатно")))
                        .collect(Collectors.toList());
            } else if ("paid".equals(pricing)) {
                filtered = filtered.stream()
                        .filter(n -> n.getTags() != null && n.getTags().stream()
                                .anyMatch(t -> t.getName().equals("Платно")))
                        .collect(Collectors.toList());
            }
        }

        // 4. Сортировка по рейтингу
        if (sortByRating) {
            filtered = filtered.stream()
                    .sorted((a, b) -> {
                        Double ratingA = (Double) neuronetService.getRatingInfo(a.getIdNeuronet())[0];
                        Double ratingB = (Double) neuronetService.getRatingInfo(b.getIdNeuronet())[0];
                        return ratingB.compareTo(ratingA);
                    })
                    .collect(Collectors.toList());
        }

        return filtered;
    }
}