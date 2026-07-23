package project;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import project.Service.UserService;

import java.util.*;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
}