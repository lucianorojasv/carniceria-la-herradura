package pe.laherradura.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.laherradura.entity.CustomerOrder;
import pe.laherradura.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findTop100ByOrderByCreatedAtDesc();
    List<CustomerOrder> findByStatusOrderByCreatedAtAsc(OrderStatus status);
    long countByStatus(OrderStatus status);
    Optional<CustomerOrder> findByCodeIgnoreCaseAndCustomer_Phone(String code, String phone);
}
