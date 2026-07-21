package project;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import project.Entity.Neuronet;
import project.Entity.Review;
import project.Service.CategoryService;
import project.Service.NeuronetService;
import project.Service.ReviewService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public String getAll(@RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false, defaultValue = "false") boolean sortByRating,
                         @RequestParam(required = false) String search,
                         @RequestParam(required = false) Boolean availableInRussia,
                         Model model) {

        List<Neuronet> neuronets;

        if (search != null && !search.isEmpty()) {
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

        model.addAttribute("neuronets", neuronets);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("sortByRating", sortByRating);
        model.addAttribute("search", search);
        model.addAttribute("availableInRussia", availableInRussia);

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

        List<Review> reviews = reviewService.getByNeuronetId(id);
        reviews.sort((a, b) -> Long.compare(
                reviewService.getVoteScore(b.getIdReview()),
                reviewService.getVoteScore(a.getIdReview())
        ));

        Map<Long, Long> voteScores = new HashMap<>();
        for (Review r : reviews) {
            voteScores.put(r.getIdReview(), reviewService.getVoteScore(r.getIdReview()));
        }

        Map<Long, Long> likeScores = new HashMap<>();
        Map<Long, Long> dislikeScores = new HashMap<>();
        for (Review r : reviews) {
            likeScores.put(r.getIdReview(), reviewService.countLikes(r.getIdReview()));
            dislikeScores.put(r.getIdReview(), reviewService.countDislikes(r.getIdReview()));
        }
        model.addAttribute("likeScores", likeScores);
        model.addAttribute("dislikeScores", dislikeScores);

        Double avgRating = reviewService.getAverageRating(id);
        Long reviewCount = reviewService.getReviewCount(id);

        model.addAttribute("neuronet", neuronet);
        model.addAttribute("reviews", reviews);
        model.addAttribute("voteScores", voteScores);
        model.addAttribute("avgRating", avgRating != null ? avgRating : 0.0);
        model.addAttribute("reviewCount", reviewCount != null ? reviewCount : 0);

        return "neuronet-detail";
    }
}