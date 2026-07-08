package project.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import project.Entity.Neuronet;
import project.Entity.User;
import project.Service.NeuronetService;
import project.Service.ReviewService;
import project.Service.UserService;

@Controller
@RequestMapping("/reviews")
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

    // Пункт 4: Поставить оценку (с комментарием или без)
    @PostMapping
    public String addReview(@RequestParam Long neuronetId,
                            @RequestParam Long userId,
                            @RequestParam int rating,
                            @RequestParam(required = false) String comment,
                            RedirectAttributes redirectAttributes) {

        try {
            Neuronet neuronet = neuronetService.getById(neuronetId)
                    .orElseThrow(() -> new IllegalArgumentException("Нейросеть не найдена"));
            User user = userService.getById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            reviewService.addReview(neuronet, user, rating, comment);
            redirectAttributes.addFlashAttribute("success", "Отзыв добавлен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/neuronets/" + neuronetId;
    }
}
