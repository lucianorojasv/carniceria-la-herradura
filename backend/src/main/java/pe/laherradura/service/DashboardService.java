package pe.laherradura.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.laherradura.dto.DashboardResponse;
import pe.laherradura.entity.CustomerOrder;
import pe.laherradura.enums.OrderStatus;
import pe.laherradura.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@Service
public class DashboardService {
 private final ProductRepository products;private final CustomerRepository customers;private final CustomerOrderRepository orders;
 public DashboardService(ProductRepository p,CustomerRepository c,CustomerOrderRepository o){products=p;customers=c;orders=o;}
 @Transactional(readOnly=true) public DashboardResponse get(){
   var ps=products.findAll();var os=orders.findTop100ByOrderByCreatedAtDesc();
   long low=ps.stream().filter(p->p.isActive()&&p.getStockQuantity().compareTo(p.getMinimumQuantity().multiply(BigDecimal.valueOf(3)))<=0).count();
   BigDecimal today=os.stream().filter(o->o.getCreatedAt().toLocalDate().equals(LocalDate.now())&&o.getStatus()!=OrderStatus.CANCELLED).map(CustomerOrder::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);
   List<DashboardResponse.RecentOrder> recent=os.stream().limit(8).map(o->new DashboardResponse.RecentOrder(o.getId(),o.getCode(),o.getCustomer().getName(),o.getStatus().name(),o.getTotal(),o.getCreatedAt().toString())).toList();
   return new DashboardResponse(ps.size(),low,customers.count(),orders.countByStatus(OrderStatus.PENDING),orders.countByStatus(OrderStatus.PREPARING),orders.countByStatus(OrderStatus.DELIVERED),today,recent);
 }
}
