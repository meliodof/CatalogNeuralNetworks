package project;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import project.Entity.Review;
import project.Entity.User;
import project.Service.ReviewService;
import project.Service.UserService;

import java.util.*;

@Controller
public class UserController {

    private final UserService userService;
    private final ReviewService reviewService;

    public UserController(UserService userService, ReviewService reviewService) {
        this.userService = userService;
        this.reviewService = reviewService;
    }

    @GetMapping("/users/register")
    public String registerForm() {
        return "users/register";
    }

    @PostMapping("/users/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают");
            return "users/register";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "Пароль должен быть не менее 6 символов");
            return "users/register";
        }
        try {
            userService.register(username, email, password);
            return "redirect:/users/login?registered=true";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "users/register";
        }
    }

    @GetMapping("/users/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "registered", required = false) String registered,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Неверное имя пользователя или пароль");
        }
        if (registered != null) {
            model.addAttribute("message", "Регистрация успешна! Войдите в аккаунт.");
        }
        return "users/login";
    }

    @GetMapping("/users/check-username")
    @ResponseBody
    public Map<String, Object> checkUsername(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();
        boolean exists = userService.getByUsername(username).isPresent();
        result.put("available", !exists);

        if (exists) {
            List<String> suggestions = generateSuggestions(username);
            result.put("suggestions", suggestions);
        }
        return result;
    }

    private List<String> generateSuggestions(String username) {
        List<String> suggestions = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 5; i++) {
            String suggestion = username;
            suggestion += random.nextInt(9999);
            suggestions.add(suggestion);
        }

        suggestions.add(username + "_official");
        suggestions.add(username + "_ai");

        return suggestions.stream()
                .filter(s -> !userService.getByUsername(s).isPresent())
                .limit(3)
                .toList();
    }

    @GetMapping("/users/check-email")
    @ResponseBody
    public Map<String, Object> checkEmail(@RequestParam String email) {
        Map<String, Object> result = new HashMap<>();
        boolean exists = userService.getByEmail(email).isPresent();
        result.put("available", !exists);
        return result;
    }

    @GetMapping("/users/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getByUsername(auth.getName()).orElseThrow();
        List<Review> reviews = reviewService.getByUserId(user.getIdUser()); // нужно создать метод
        model.addAttribute("reviews", reviews);
        return "users/profile";
    }

    @GetMapping("/users/settings")
    public String settingsForm() {
        return "users/settings";
    }

    @PostMapping("/users/settings/email")
    public String changeEmail(@RequestParam String newEmail, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            userService.changeEmail(auth.getName(), newEmail);
            model.addAttribute("success", "Email успешно изменён");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "users/settings";
    }

    @PostMapping("/users/settings/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают");
            return "users/settings";
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            userService.changePassword(auth.getName(), currentPassword, newPassword);
            model.addAttribute("success", "Пароль успешно изменён");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "users/settings";
    }
}