package project.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.Entity.Category;
import project.Repository.RepCategory;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final RepCategory repCategory;

    public CategoryService(RepCategory repCategory){
        this.repCategory=repCategory;
    }

    public List<Category> getAll() {
        return repCategory.findAll();
    }

    public Optional<Category> getById(Long id) {
        return repCategory.findById(id);
    }

    @Transactional
    public Category save(Category category) {
        return repCategory.save(category);
    }

    @Transactional
    public void delete(Long id) {
        repCategory.deleteById(id);
    }

}
