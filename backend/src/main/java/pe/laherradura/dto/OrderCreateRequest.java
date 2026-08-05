package pe.laherradura.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import pe.laherradura.enums.*;
import java.math.BigDecimal;
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
 @NotEmpty List<@Valid Item> items
) {
 public record Item(@NotNull Long productId, @NotNull @DecimalMin("0.01") BigDecimal quantity) {}
}
