package pe.laherradura.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.laherradura.entity.CustomerOrder;
import java.util.List;
import pe.laherradura.enums.OrderStatus;
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findTop100ByOrderByCreatedAtDesc();
    List<CustomerOrder> findByStatusOrderByCreatedAtAsc(OrderStatus status);
    long countByStatus(OrderStatus status);
}
