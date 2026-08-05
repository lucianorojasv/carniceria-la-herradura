package pe.laherradura.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.laherradura.entity.Promotion;
import java.util.List;
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByActiveTrueOrderByStartDateDesc();
}
