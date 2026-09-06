package project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import project.DTO.CategoryGroupDto;
import project.DTO.NeuronetCardDto;
import project.DTO.NeuronetNameDto;
import project.Entity.Category;
import project.Entity.Neuronet;
import project.Service.CategoryService;
import project.Service.ChatService;
import project.Service.NeuronetService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private static final int DEFAULT_PAGE_SIZE = 12;

    private final NeuronetService neuronetService;
    private final CategoryService categoryService;
    private final ChatService chatService;


    public HomeController(NeuronetService neuronetService, CategoryService categoryService, ChatService chatService) {
        this.neuronetService = neuronetService;
        this.categoryService = categoryService;
        this.chatService = chatService;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean availableInRussia,
            @RequestParam(required = false, defaultValue = "false") boolean sortByRating,
            @RequestParam(required = false) String pricing,
            @RequestParam(required = false) String aiQuery,
            HttpServletRequest request,
            Model model) {

        // Данные для инлайн-подсказки: имя + количество отзывов
        List<NeuronetNameDto> namesWithReviews = neuronetService.getNeuronetNamesWithReviews();
        model.addAttribute("neuronetNamesData", namesWithReviews);

        // ===== AI-фильтр =====
        List<NeuronetCardDto> aiFiltered = null;
        if (aiQuery != null && !aiQuery.isBlank()) {
            ChatService.ChatResult chatResult = chatService.chat(aiQuery);
            if (chatResult.recommendations() != null && !chatResult.recommendations().isEmpty()) {
                List<String> names = chatResult.recommendations().stream()
                        .map(ChatService.Recommendation::name)
                        .toList();
                List<Neuronet> neuronets = neuronetService.findByNames(names);
                aiFiltered = neuronets.stream()
                        .map(this::toCardDto)
                        .toList();
                // Группировка по категориям
                Map<String, List<NeuronetCardDto>> grouped = aiFiltered.stream()
                        .collect(Collectors.groupingBy(
                                n -> n.getCategoryName() != null ? n.getCategoryName() : "Без категории",
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));
                model.addAttribute("groupedNeuronets", grouped.entrySet().stream()
                        .map(e -> new CategoryGroupDto(e.getKey(), e.getValue()))
                        .toList());
                model.addAttribute("totalResults", aiFiltered.size());
                model.addAttribute("aiQuery", aiQuery);
            } else {
                model.addAttribute("groupedNeuronets", List.of());
                model.addAttribute("totalResults", 0);
                model.addAttribute("aiQuery", aiQuery);
            }
        }

        if (aiQuery == null || aiQuery.isBlank()) {
            // Обычная фильтрация
            Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE, Sort.by("name").ascending());
            Page<NeuronetCardDto> neuronetPage;
            
            if (search != null && !search.isBlank()) {
                // Поиск — без пагинации, возвращаем все результаты
                List<Neuronet> results = neuronetService.search(search);
                List<NeuronetCardDto> cardDtos = results.stream()
                        .map(this::toCardDto)
                        .toList();
                Map<String, List<NeuronetCardDto>> grouped = cardDtos.stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                                n -> n.getCategoryName() != null ? n.getCategoryName() : "Без категории",
                                java.util.LinkedHashMap::new,
                                java.util.stream.Collectors.toList()
                        ));
                model.addAttribute("groupedNeuronets", grouped.entrySet().stream()
                        .map(e -> new CategoryGroupDto(e.getKey(), e.getValue()))
                        .toList());
                model.addAttribute("totalResults", cardDtos.size());
            } else {
                neuronetPage = neuronetService.getAllPaged(page, DEFAULT_PAGE_SIZE,
                        categoryId, availableInRussia, pricing, sortByRating);
                // Группировка по категориям для шаблона
                Map<String, List<NeuronetCardDto>> grouped = neuronetPage.getContent().stream()
                        .collect(Collectors.groupingBy(
                                n -> n.getCategoryName() != null ? n.getCategoryName() : "Без категории",
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));
                model.addAttribute("groupedNeuronets", grouped.entrySet().stream()
                        .map(e -> new CategoryGroupDto(e.getKey(), e.getValue()))
                        .toList());
                model.addAttribute("totalResults", neuronetPage.getTotalElements());
            }
        }

        // Категории
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("search", search);
        model.addAttribute("availableInRussia", availableInRussia);
        model.addAttribute("sortByRating", sortByRating);
        model.addAttribute("pricing", pricing);

        // Данные для autocomplete
        List<String> allNames = neuronetService.getAll().stream()
                .map(Neuronet::getName)
                .distinct()
                .sorted()
                .toList();
        model.addAttribute("neuronetNames", allNames);

        // Если это HTMX-запрос — возвращаем только фрагмент <main>, а не всю страницу
        boolean isHtmx = "true".equals(request.getHeader("HX-Request"));
        if (isHtmx) {
            return "fragments/main-area :: mainArea";
        }

        return "index";
    }

    private NeuronetCardDto toCardDto(Neuronet n) {
        List<String> tagNames = n.getTags() != null
                ? n.getTags().stream().map(tag -> tag.getName()).toList()
                : List.of();
        String categoryName = n.getCategory() != null ? n.getCategory().getName() : null;
        Double avg = neuronetService.getRatingInfo(n.getIdNeuronet()).getAverageRating();
        Long count = neuronetService.getRatingInfo(n.getIdNeuronet()).getReviewCount();
        
        return new NeuronetCardDto(
                n.getIdNeuronet(), n.getName(), n.getDescriptionNetwork(),
                n.getNeuronetIcon(), n.getAvailableInRussia(),
                categoryName, tagNames, avg, count
        );
    }
}