package project;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import project.Entity.Neuronet;
import project.Service.CategoryService;
import project.Service.NeuronetService;

import java.util.List;

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
        } else if (sortByRating) {
            neuronets = neuronetService.getAllSortedByRating();
        } else {
            neuronets = neuronetService.getAll();
        }

        model.addAttribute("topNeuronets", neuronetService.getTopPopular(5));
        model.addAttribute("neuronets", neuronets);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("search", search);
        model.addAttribute("availableInRussia", availableInRussia);
        model.addAttribute("sortByRating", sortByRating);

        return "index";
    }
}