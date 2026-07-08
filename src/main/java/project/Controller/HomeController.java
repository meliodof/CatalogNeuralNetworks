package project.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import project.Service.NeuronetService;

public class HomeController {
    private final NeuronetService neuronetService;

    public HomeController(NeuronetService neuronetService) {
        this.neuronetService = neuronetService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("topNeuronets", neuronetService.getTopPopular(10));
        return "index";

    }
}
