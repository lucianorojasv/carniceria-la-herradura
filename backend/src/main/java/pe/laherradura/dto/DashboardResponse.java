package pe.laherradura.dto;
import java.math.BigDecimal;
import java.util.List;
public record DashboardResponse(long totalProducts, long lowStockProducts, long totalCustomers,
 long pendingOrders, long preparingOrders, long deliveredOrders, BigDecimal salesToday,
 List<RecentOrder> recentOrders) {
 public record RecentOrder(Long id, String code, String customer, String status, BigDecimal total, String createdAt) {}
}
