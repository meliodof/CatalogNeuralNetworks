package project;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import project.Entity.Category;
import project.Entity.Neuronet;
import project.Entity.Review;
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

        List<Neuronet> all = neuronetService.getAll();
        // Данные для инлайн-подсказки: имя + количество отзывов
        List<Map<String, Object>> namesWithReviews = neuronetService.getAll().stream()
                .map(n -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("name", n.getName());
                    map.put("reviews", neuronetService.getRatingInfo(n.getIdNeuronet())[1]); // [0]=avg, [1]=count
                    return map;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("reviews"), (Long) a.get("reviews")))
                .toList();
        model.addAttribute("neuronetNamesData", namesWithReviews);

        if (availableInRussia != null) {
            all = all.stream().filter(n -> n.getAvailableInRussia() == availableInRussia).toList();
        }
        if (pricing != null) {
            if ("free".equals(pricing)) {
                all = all.stream().filter(n -> n.getTags().stream().anyMatch(t -> t.getName().equals("Бесплатно"))).toList();
            } else if ("paid".equals(pricing)) {
                all = all.stream().filter(n -> n.getTags().stream().anyMatch(t -> t.getName().equals("Платно"))).toList();
            }
        }
        if (sortByRating) {
            all = all.stream().sorted((a, b) -> {
                Double ra = (Double) neuronetService.getRatingInfo(a.getIdNeuronet())[0];
                Double rb = (Double) neuronetService.getRatingInfo(b.getIdNeuronet())[0];
                return rb.compareTo(ra);
            }).toList();
        }
        if (search != null && !search.isBlank()) {
            all = neuronetService.search(search);
        }

        Map<String, List<Neuronet>> grouped = new LinkedHashMap<>();
        Map<Category, List<Neuronet>> byCategory = all.stream()
                .collect(Collectors.groupingBy(Neuronet::getCategory, LinkedHashMap::new, Collectors.toList()));
        byCategory.forEach((cat, list) -> grouped.put(cat.getName(), list));

        // Список названий для автодополнения
        List<String> allNames = neuronetService.getAll().stream()
                .map(Neuronet::getName)
                .distinct()
                .sorted()
                .toList();

        model.addAttribute("neuronetNames", allNames);
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
}