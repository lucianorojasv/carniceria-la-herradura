package pe.laherradura.dto;
import jakarta.validation.constraints.NotNull;
import pe.laherradura.enums.OrderStatus;
public record OrderStatusRequest(@NotNull OrderStatus status) {}
