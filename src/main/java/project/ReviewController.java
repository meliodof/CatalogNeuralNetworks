package project;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.DTO.AddReviewRequest;
import project.DTO.ReviewWithVotesDto;
import project.Entity.Neuronet;
import project.Entity.User;
import project.Service.NeuronetService;
import project.Service.ReviewService;
import project.Service.UserService;

import java.util.List;

@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final NeuronetService neuronetService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService,
                            NeuronetService neuronetService,
                            UserService userService) {
        this.reviewService = reviewService;
        this.neuronetService = neuronetService;
        this.userService = userService;
    }

    @PostMapping("/reviews")
    public String addReview(@Valid @ModelAttribute AddReviewRequest request,
                            Errors errors,
                            Model model) {
        // Получаем текущего пользователя
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Neuronet neuronet = neuronetService.getById(request.getNeuronetId())
                .orElseThrow(() -> new RuntimeException("Нейросеть не найдена"));

        reviewService.addReview(neuronet, user, request.getRating(), request.getComment());

        return getReviewsFragment(request.getNeuronetId(), model);
    }

    @PostMapping("/reviews/vote")
    public String vote(@RequestParam Long reviewId,
                       @RequestParam Long userId,
                       @RequestParam int vote,
                       @RequestParam Long neuronetId,
                       Model model) {
        reviewService.vote(reviewId, userId, vote);
        
        return getReviewsFragment(neuronetId, model);
    }

    private String getReviewsFragment(Long neuronetId, Model model) {
        Neuronet neuronet = neuronetService.getById(neuronetId)
                .orElseThrow(() -> new RuntimeException("Нейросеть не найдена"));
        
        List<ReviewWithVotesDto> reviewsWithVotes = reviewService.getReviewsWithVotes(neuronetId);

        model.addAttribute("neuronet", neuronet);
        model.addAttribute("reviews", reviewsWithVotes);
        model.addAttribute("avgRating", reviewService.getAverageRating(neuronetId) != null 
                ? reviewService.getAverageRating(neuronetId) : 0.0);
        model.addAttribute("reviewCount", reviewService.getReviewCount(neuronetId) != null 
                ? reviewService.getReviewCount(neuronetId) : 0L);

        return "fragments/reviews :: reviews";
    }
}