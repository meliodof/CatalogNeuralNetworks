package project.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import project.DTO.NeuronetCardDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.DTO.*;
import project.Entity.Neuronet;
import project.Entity.Tag;
import project.Repository.RepNeuronet;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class NeuronetService {

    private static final int DEFAULT_PAGE_SIZE = 12;

    private final RepNeuronet repNeuronet;
    private final ReviewService reviewService;

    public NeuronetService(RepNeuronet repNeuronet, ReviewService reviewService) {
        this.repNeuronet = repNeuronet;
        this.reviewService = reviewService;
    }

    // ===== Пагинация с фильтрами (N+1 FIX + пагинация + hardcoded FIX) =====

    public Page<NeuronetCardDto> getAllPaged(int page, int size,
                                              Long categoryId,
                                              Boolean availableInRussia,
                                              String pricing,
                                              boolean sortByRating) {
        Pageable pageable = createPageable(page, size, sortByRating);

        Page<Neuronet> result;
        if (categoryId != null || availableInRussia != null) {
            result = repNeuronet.findAllWithFilters(categoryId, availableInRussia, pageable);
        } else if (sortByRating) {
            Page<Neuronet> paged = repNeuronet.findAllPaged(pageable);
            result = paged;
        } else {
            result = repNeuronet.findAllWithCategoryAndTags(pageable);
        }

        if (pricing != null && !pricing.isBlank()) {
            List<String> targetTags = resolvePricingTagNames(pricing);
            List<NeuronetCardDto> filtered = result.getContent().stream()
                    .filter(n -> n.getTags() != null && !n.getTags().isEmpty() &&
                            n.getTags().stream().anyMatch(t -> targetTags.contains(t.getName())))
                    .map(n -> mapToCardDto(n))
                    .collect(java.util.stream.Collectors.toList());
            Page<NeuronetCardDto> pageResult = new PageImpl<>(filtered, pageable, filtered.size());
            return pageResult;
        }

        return result.map(n -> mapToCardDto(n));
    }

    public List<Neuronet> getAll() {
        return repNeuronet.findAllWithCategoryAndTags();
    }

    public List<Neuronet> search(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        return repNeuronet.searchWithCategoryAndTags(query);
    }

    public List<Neuronet> getByCategoryId(Long categoryId) {
        if (categoryId == null) {
            return Collections.emptyList();
        }
        return repNeuronet.findByCategory_IdCategoriesWithTags(categoryId);
    }

    public Page<Neuronet> getByCategoryIdPaged(Long categoryId, int page, int size) {
        if (categoryId == null) {
            return Page.empty();
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return repNeuronet.findByCategory_IdCategoriesWithTags(categoryId, pageable);
    }

    public List<Neuronet> getByAvailableInRussia(Boolean available) {
        if (available == null) {
            return Collections.emptyList();
        }
        return repNeuronet.findByAvailableInRussiaWithAll(available);
    }

    public Page<Neuronet> getByAvailableInRussiaPaged(Boolean available, int page, int size) {
        if (available == null) {
            return Page.empty();
        }
        Pageable pageable = PageRequest.of(page, size);
        return repNeuronet.findByAvailableInRussiaWithAll(available, pageable);
    }

    public List<Neuronet> getByCategoryAndAvailability(Long categoryId, Boolean available) {
        if (categoryId == null || available == null) {
            return Collections.emptyList();
        }
        return repNeuronet.findByCategoryAndAvailability(categoryId, available);
    }

    public Optional<Neuronet> getById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return repNeuronet.findById(id);
    }

    // ===== Топ популярных (N+1 FIX — сортировка через JPQL) =====

    public List<Neuronet> getTopPopular(int limit) {
        return repNeuronet.findTopPopular(limit, 0);
    }

    // ===== Рейтинг (null-safe) =====

    public RatingInfoDto getRatingInfo(Long idNeuronet) {
        if (idNeuronet == null) {
            return new RatingInfoDto(0.0, 0L);
        }
        Double avg = reviewService.getAverageRating(idNeuronet);
        Long count = reviewService.getReviewCount(idNeuronet);
        return new RatingInfoDto(avg, count);
    }

    public List<NeuronetNameDto> getNeuronetNamesWithReviews() {
        List<Neuronet> all = repNeuronet.findAllWithCategoryAndTags();
        return all.stream()
                .map(n -> {
                    RatingInfoDto info = getRatingInfo(n.getIdNeuronet());
                    return new NeuronetNameDto(n.getName(), info.getReviewCount());
                })
                .sorted(Comparator.comparingLong(NeuronetNameDto::getReviewCount).reversed())
                .toList();
    }

    // ===== Группировка по категориям =====

    public Map<String, List<Neuronet>> groupByCategory(List<Neuronet> neuronets) {
        return neuronets.stream()
                .collect(Collectors.groupingBy(
                        n -> n.getCategory() != null ? n.getCategory().getName() : "Без категории",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    // ===== CRUD =====

    @Transactional
    public Neuronet save(Neuronet neuronet) {
        return repNeuronet.save(neuronet);
    }

    @Transactional
    public void delete(Long id) {
        repNeuronet.deleteById(id);
    }

    // ===== Вспомогательные методы =====

    private Pageable createPageable(int page, int size, boolean sortByRating) {
        if (sortByRating) {
            return PageRequest.of(page, size, Sort.by("idNeuronet").ascending());
        }
        return PageRequest.of(page, size, Sort.by("name").ascending());
    }

    private List<String> resolvePricingTagNames(String pricing) {
        if (pricing == null || pricing.isBlank()) {
            return Collections.emptyList();
        }
        return switch (pricing) {
            case "free" -> List.of("Бесплатно");
            case "freemium" -> List.of("Условно-бесплатно", "Пробный период");
            case "paid" -> List.of("Платно");
            default -> Collections.emptyList();
        };
    }

    private NeuronetCardDto mapToCardDto(Neuronet n) {
        List<String> tagNames = n.getTags() != null
                ? n.getTags().stream().map(Tag::getName).toList()
                : Collections.emptyList();

        String categoryName = n.getCategory() != null ? n.getCategory().getName() : null;
        Double avg = reviewService.getAverageRating(n.getIdNeuronet());
        Long count = reviewService.getReviewCount(n.getIdNeuronet());

        return new NeuronetCardDto(
                n.getIdNeuronet(),
                n.getName(),
                n.getDescriptionNetwork(),
                n.getNeuronetIcon(),
                n.getAvailableInRussia(),
                categoryName,
                tagNames,
                avg,
                count
        );
    }
}

