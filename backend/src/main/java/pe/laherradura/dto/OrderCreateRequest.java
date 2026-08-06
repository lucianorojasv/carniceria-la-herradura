package pe.laherradura.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import pe.laherradura.enums.FulfillmentType;
import pe.laherradura.enums.OrderSource;
import pe.laherradura.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderCreateRequest(
        @NotBlank String customerName,
        @NotBlank String customerPhone,
        FulfillmentType fulfillmentType,
        Long deliveryZoneId,
        String deliveryAddress,
        String deliveryReference,
        PaymentMethod paymentMethod,
        OrderSource source,
        String notes,
        OffsetDateTime scheduledFor,
        @NotEmpty List<@Valid Item> items
) {
    public record Item(@NotNull Long productId, @NotNull @DecimalMin("0.01") BigDecimal quantity) { }
}
