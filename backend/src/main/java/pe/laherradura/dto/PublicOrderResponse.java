package pe.laherradura.dto;

import pe.laherradura.entity.CustomerOrder;
import pe.laherradura.enums.FulfillmentType;
import pe.laherradura.enums.OrderStatus;
import pe.laherradura.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PublicOrderResponse(
        String code,
        OrderStatus status,
        FulfillmentType fulfillmentType,
        PaymentMethod paymentMethod,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal total,
        OffsetDateTime scheduledFor,
        OffsetDateTime createdAt,
        List<Item> items
) {
    public static PublicOrderResponse from(CustomerOrder order) {
        return new PublicOrderResponse(
                order.getCode(),
                order.getStatus(),
                order.getFulfillmentType(),
                order.getPaymentMethod(),
                order.getSubtotal(),
                order.getDeliveryFee(),
                order.getTotal(),
                order.getScheduledFor(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(item -> new Item(item.getProductName(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal()))
                        .toList()
        );
    }

    public record Item(String productName, BigDecimal quantity, BigDecimal unitPrice, BigDecimal subtotal) { }
}
