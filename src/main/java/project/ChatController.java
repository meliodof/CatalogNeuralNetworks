package project;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.Service.ChatService;
import project.Service.NeuronetService;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final NeuronetService neuronetService;

    public ChatController(ChatService chatService, NeuronetService neuronetService) {
        this.chatService = chatService;
        this.neuronetService = neuronetService;
    }

    /**
     * Страница чата
     */
    @GetMapping("/ask")
    public String chatPage() {
        return "chat/ask";
    }

    /**
     * Обработка запроса (HTMX или обычная форма)
     */
    @PostMapping("/query")
    public String query(
            @RequestParam String message,
            Model model,
            HttpServletRequest request) {

        if (message == null || message.isBlank()) {
            model.addAttribute("error", "Введите ваш вопрос");
            return "fragments/chat :: chat-result";
        }

        // Вызываем AI-бот
        ChatService.ChatResult result = chatService.chat(message);

        model.addAttribute("message", message);
        model.addAttribute("explanation", result.explanation());
        model.addAttribute("recommendations", result.recommendations());

        // HTMX-ответ — только фрагмент
        boolean isHtmx = "true".equals(request.getHeader("HX-Request"));
        if (isHtmx) {
            return "fragments/chat :: chat-result";
        }

        return "chat/result";
    }

    /**
     * Админ-кнопка: загрузить каталог в векторную БД
     */
    @GetMapping("/admin/load")
    @ResponseBody
    public String loadCatalog() {
        try {
            chatService.loadCatalogToVectorStore();
            return "Каталог загружен в векторную БД!";
        } catch (Exception e) {
            return "Ошибка загрузки: " + e.getMessage();
        }
    }
}
