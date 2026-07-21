package project;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.Entity.Neuronet;
import project.Entity.Review;
import project.Entity.User;
import project.Service.NeuronetService;
import project.Service.ReviewService;
import project.Service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public String addReview(@RequestParam Long neuronetId,
                            @RequestParam Long userId,
                            @RequestParam int rating,
                            @RequestParam(required = false) String comment,
                            Model model) {
        Neuronet neuronet = neuronetService.getById(neuronetId)
                .orElseThrow(() -> new RuntimeException("Нейросеть не найдена"));
        User user = userService.getById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        reviewService.addReview(neuronet, user, rating, comment);

        return getReviewsFragment(neuronetId, model);
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
        List<Review> reviews = reviewService.getByNeuronetId(neuronetId);
        reviews.sort((a, b) -> Long.compare(
                reviewService.getVoteScore(b.getIdReview()),
                reviewService.getVoteScore(a.getIdReview())
        ));
        Map<Long, Long> likeScores = new HashMap<>();
        Map<Long, Long> dislikeScores = new HashMap<>();
        for (Review r : reviews) {
            likeScores.put(r.getIdReview(), reviewService.countLikes(r.getIdReview()));
            dislikeScores.put(r.getIdReview(), reviewService.countDislikes(r.getIdReview()));
        }
        model.addAttribute("neuronet", neuronet);
        model.addAttribute("reviews", reviews);
        model.addAttribute("likeScores", likeScores);
        model.addAttribute("dislikeScores", dislikeScores);
        model.addAttribute("avgRating", reviewService.getAverageRating(neuronetId));
        model.addAttribute("reviewCount", reviewService.getReviewCount(neuronetId));

        return "fragments/reviews :: reviews";
    }
}