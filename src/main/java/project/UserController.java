package project;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.Entity.User;
import project.Service.UserService;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Форма регистрации
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "users/register";
    }

    // Регистрация
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           Model model) {
        try {
            userService.register(username, email, password);
            return "redirect:/login";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "users/register";
        }
    }

    // Профиль пользователя
    @GetMapping("/{id}")
    public String profile(@PathVariable Long id, Model model) {
        return userService.getById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    return "users/profile";
                })
                .orElse("redirect:/neuronets");
    }
}
