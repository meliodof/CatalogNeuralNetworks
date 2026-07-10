package project.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.Entity.Neuronet;
import project.Entity.Tag;
import project.Repository.RepNeuronet;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class NeuronetService {
    private final RepNeuronet repNeuronet;
    private final ReviewService reviewService;

    public NeuronetService(RepNeuronet repNeuronet, ReviewService reviewService) {
        this.repNeuronet = repNeuronet;
        this.reviewService = reviewService;
    }

    // Пункт 1: Весь список
    public List<Neuronet> getAll() {
        return repNeuronet.findAll();
    }

    // Пункт 2: По категории
    public List<Neuronet> getByCategoryId(Long categoryId) {
        return repNeuronet.findByCategory_IdCategories(categoryId);
    }

    // Пункт 3: Карточка нейросети
    public Optional<Neuronet> getById(Long id) {
        return repNeuronet.findById(id);
    }

    // Пункт 5: Сортировка по рейтингу — без DTO, через сервис отзывов
    public List<Neuronet> getAllSortedByRating() {
        List<Neuronet> all = repNeuronet.findAll();

        return all.stream()
                .sorted(Comparator
                        .comparing((Neuronet n) -> reviewService.getAverageRating(n.getIdNeuronet()),
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(n -> reviewService.getReviewCount(n.getIdNeuronet()),
                                Comparator.reverseOrder()))
                .toList();
    }

    // Пункт 6: Поисковая строка
    public List<Neuronet> search(String query) {
        return repNeuronet
                .findByNameContainingIgnoreCaseOrDescriptionNetworkContainingIgnoreCase(query, query);
    }

    // Пункт 7: Топ популярных
    public List<Neuronet> getTopPopular(int limit) {
        return getAllSortedByRating().stream()
                .limit(limit)
                .toList();
    }

    // Пункт 8: Фильтр по доступности в России
    public List<Neuronet> getByAvailableInRussia(boolean available) {
        return repNeuronet.findByAvailableInRussia(available);
    }

    // Фильтр по тегу
    public List<Neuronet> getByTag(Tag tag) {
        return repNeuronet.findAll().stream()
                .filter(n -> n.getTags().contains(tag))
                .toList();
    }

    // Комбинированный фильтр: доступность + категория
    public List<Neuronet> getByCategoryAndAvailability(Long categoryId, boolean available) {
        return repNeuronet.findAll().stream()
                .filter(n -> n.getCategory().getIdCategories().equals(categoryId))
                .filter(n -> n.getAvailableInRussia() == available)
                .toList();
    }

    public Object[] getRatingInfo(Long idNeuronet) {
        Double avg = reviewService.getAverageRating(idNeuronet);
        Long count = reviewService.getReviewCount(idNeuronet);
        return new Object[]{avg != null ? avg : 0.0, count != null ? count : 0L};
    }


    @Transactional
    public Neuronet save(Neuronet neuronet) {
        return repNeuronet.save(neuronet);
    }

    @Transactional
    public void delete(Long id) {
        repNeuronet.deleteById(id);
    }

}

