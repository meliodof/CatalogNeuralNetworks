package project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.Entity.Category;

import java.util.List;

public interface RepCategory extends JpaRepository<Category,Long>{
    List<Category> findAllByOrderByNameAsc();
}
