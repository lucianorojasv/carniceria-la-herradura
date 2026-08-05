package pe.laherradura.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.laherradura.entity.DeliveryZone;
import java.util.List;
import java.util.Optional;
public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, Long> {
    List<DeliveryZone> findByActiveTrueOrderByNameAsc();
    Optional<DeliveryZone> findByNameIgnoreCase(String name);
}
