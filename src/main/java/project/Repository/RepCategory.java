package project.Repository;

import project.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepCategory extends JpaRepository<Category,Long>{
}
