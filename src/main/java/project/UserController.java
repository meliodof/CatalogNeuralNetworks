package project;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import project.DTO.AddReviewRequest;
import project.DTO.ChangeEmailRequest;
import project.DTO.ChangePasswordRequest;
import project.DTO.RegisterRequest;
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
    public String registerForm(@ModelAttribute("registerRequest") RegisterRequest request) {
        return "users/register";
    }

    @PostMapping("/users/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                            Errors errors,
                            Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("error", errors.getAllErrors().get(0).getDefaultMessage());
            return "users/register";
        }
        
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("error", "Пароли не совпадают");
            return "users/register";
        }
        
        try {
            userService.register(request);
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
        List<Review> reviews = reviewService.getByUserId(user.getIdUser());
        model.addAttribute("reviews", reviews);
        return "users/profile";
    }

    @GetMapping("/users/settings")
    public String settingsForm(@ModelAttribute("changeEmailRequest") ChangeEmailRequest emailRequest,
                               @ModelAttribute("changePasswordRequest") ChangePasswordRequest passwordRequest) {
        return "users/settings";
    }

    @PostMapping("/users/settings/email")
    public String changeEmail(@Valid @ModelAttribute("changeEmailRequest") ChangeEmailRequest request,
                              Errors errors,
                              Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("error", errors.getAllErrors().get(0).getDefaultMessage());
            return "users/settings";
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            userService.changeEmail(auth.getName(), request);
            model.addAttribute("success", "Email успешно изменён");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "users/settings";
    }

    @PostMapping("/users/settings/password")
    public String changePassword(@Valid @ModelAttribute("changePasswordRequest") ChangePasswordRequest request,
                                  Errors errors,
                                  Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("error", errors.getAllErrors().get(0).getDefaultMessage());
            return "users/settings";
        }
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("error", "Пароли не совпадают");
            return "users/settings";
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            userService.changePassword(auth.getName(), request);
            model.addAttribute("success", "Пароль успешно изменён");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "users/settings";
    }
}