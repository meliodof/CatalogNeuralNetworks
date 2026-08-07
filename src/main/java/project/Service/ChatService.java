package project.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.Entity.Neuronet;
import project.Repository.RepNeuronet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
        Ты — AI-помощник каталога нейросетей CatalogNeuralNetworks.
        
        Твоя задача — помогать пользователям выбирать нейросети на основе их потребностей.
        
        Правила:
        1. Отвечай на русском языке.
        2. Рекомендуй ТОЛЬКО нейросети из предоставленного контекста.
        3. Давай краткое объяснение, почему каждая нейросеть подходит.
        4. Указывай название нейросети, основную причину рекомендации и цену (если известна).
        5. Если пользователь не указал потребность — уточни, что именно он хочет делать.
        6. Формат ответа — JSON с полями:
           - explanation: строка с объяснением
           - recommendations: массив объектов {name, reason, price}
        7. Не выдумывай нейросети, которых нет в контексте!
        """;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final RepNeuronet neuronetRepository;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       VectorStore vectorStore,
                       RepNeuronet neuronetRepository) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.neuronetRepository = neuronetRepository;
        log.info("ChatService инициализирован успешно");
    }

    /**
     * Основной метод чата — обрабатывает запрос пользователя
     */
    public ChatResult chat(String userMessage) {
        log.info("Получен запрос: {}", userMessage);
        
        try {
            // 1. Ищем похожие нейросети через векторный поиск
            List<Document> similarDocs = searchNeuronets(userMessage);
            log.info("Найдено документов: {}", similarDocs.size());

            // 2. Формируем контекст из найденных документов
            String context = buildContext(similarDocs);

            // 3. Запрашиваем LLM
            String llmResponse = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userMessage)
                    .user("Контекст — доступные нейросети:\n" + context)
                    .call()
                    .content();

            log.info("Ответ от LLM получен, длина: {}", llmResponse.length());

            // 4. Парсим JSON-ответ
            return parseLlmResponse(llmResponse, userMessage);

        } catch (Exception e) {
            log.error("Ошибка при обработке запроса", e);
            return new ChatResult(userMessage, "⚠️ Ошибка: " + e.getMessage() + " — проверьте логи сервера и убедитесь, что pgvector настроен и каталог загружен через /chat/admin/load", List.of());
        }
    }

    /**
     * Векторный поиск нейросетей по запросу пользователя
     */
    private List<Document> searchNeuronets(String query) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(5)
                .build();
        return vectorStore.similaritySearch(request);
    }

    /**
     * Формирует текстовый контекст из документов
     */
    private String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "Нейросети не найдены в базе.";
        }

        return documents.stream()
                .map(doc -> {
                    Map<String, Object> metadata = doc.getMetadata();
                    String name = (String) metadata.get("name");
                    String description = doc.getText();
                    String price = (String) metadata.get("price");
                    String category = (String) metadata.get("category");
                    String rating = (String) metadata.get("rating");

                    return String.format(
                            "• %s (категория: %s, рейтинг: %s, цена: %s)\n  Описание: %s",
                            name, category, rating, price, description
                    );
                })
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * Парсит JSON-ответ от LLM
     */
    private ChatResult parseLlmResponse(String response, String userQuery) {
        try {
            // Извлекаем JSON из ответа (иногда LLM добавляет текст вокруг)
            String json = extractJson(response);

            // Простой парсинг JSON (в реальном проекте — Jackson)
            String explanation = extractField(json, "explanation");
            String recommendations = extractJsonArray(json, "recommendations");

            List<Recommendation> recs = parseRecommendations(recommendations);

            return new ChatResult(userQuery, explanation, recs);

        } catch (Exception e) {
            // Fallback — возвращаем сырой ответ
            return new ChatResult(userQuery, response, List.of());
        }
    }

    private String extractJson(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start != -1 && end != -1) {
            return response.substring(start, end + 1);
        }
        return response;
    }

    private String extractField(String json, String field) {
        String pattern = "\"" + field + "\":";
        int start = json.indexOf(pattern) + pattern.length();
        int end = json.indexOf(',', start);
        if (end == -1) end = json.indexOf('}', start);
        if (end == -1) return "";

        String value = json.substring(start, end).trim();
        // Убираем кавычки
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * Извлекает JSON-массив для указанного поля
     */
    private String extractJsonArray(String json, String field) {
        String pattern = "\"" + field + "\":";
        int start = json.indexOf(pattern) + pattern.length();
        // Ищем открывающую скобку массива
        start = json.indexOf('[', start);
        if (start == -1) return "[]";
        // Ищем закрывающую скобку
        int end = json.indexOf(']', start);
        if (end == -1) return "[]";
        return json.substring(start, end + 1);
    }

    private List<Recommendation> parseRecommendations(String json) {
        List<Recommendation> result = new ArrayList<>();
        // Простой парсинг — в реальном проекте использовать Jackson ObjectMapper
        String[] items = json.split("\\},\\s*\\{");
        for (String item : items) {
            item = item.replaceAll("[{}]", "");
            String name = extractField(item, "name");
            String reason = extractField(item, "reason");
            String price = extractField(item, "price");
            result.add(new Recommendation(name, reason, price));
        }
        return result;
    }

    /**
     * Загружает все нейросети из каталога в векторную БД
     * Вызывается один раз при запуске или админом
     */
    @Transactional
    public void loadCatalogToVectorStore() {
        List<Neuronet> allNeuronets = neuronetRepository.findAllWithCategoryAndTags();

        List<Document> documents = allNeuronets.stream()
                .map(this::toDocument)
                .toList();

        vectorStore.add(documents);
    }

    /**
     * Преобразует нейросеть в документ для векторной БД
     */
    private Document toDocument(Neuronet neuronet) {
        String text = String.format(
                "Название: %s\nКатегория: %s\nОписание: %s\nРасширенное описание: %s\n" +
                "Использование: регистрация — %s, настройка — %s, общее — %s\n" +
                "Официальный сайт: %s\nAPI: %s\nGitHub: %s\nЦена: %s",
                neuronet.getName(),
                neuronet.getCategory() != null ? neuronet.getCategory().getName() : "Без категории",
                neuronet.getDescriptionNetwork(),
                neuronet.getExtendedDescription(),
                neuronet.getUsageRegistration(),
                neuronet.getUsageSetup(),
                neuronet.getUsageGeneral(),
                neuronet.getOfficialUrl(),
                neuronet.getApiUrl(),
                neuronet.getGithubUrl(),
                neuronet.getPricingInfo() != null ? neuronet.getPricingInfo() : "Не указано"
        );

        return Document.builder()
                .text(text)
                .metadata(Map.of(
                        "name", neuronet.getName(),
                        "category", neuronet.getCategory() != null ? neuronet.getCategory().getName() : "Без категории",
                        "price", neuronet.getPricingInfo() != null ? neuronet.getPricingInfo() : "Не указано",
                        "rating", "—",
                        "id", String.valueOf(neuronet.getIdNeuronet())
                ))
                .build();
    }

    // ==================== DTO ====================

    public record ChatResult(
            String userMessage,
            String explanation,
            List<Recommendation> recommendations
    ) {}

    public record Recommendation(
            String name,
            String reason,
            String price
    ) {}
}
