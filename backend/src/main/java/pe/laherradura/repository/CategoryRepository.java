package pe.laherradura.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.laherradura.entity.Category;
import java.util.List;
import java.util.Optional;
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByActiveTrueOrderByNameAsc();
    Optional<Category> findByNameIgnoreCase(String name);
}
