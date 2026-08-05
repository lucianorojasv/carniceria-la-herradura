package pe.laherradura.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record DeliveryZoneRequest(@NotBlank String name, @NotNull @DecimalMin("0") BigDecimal fee,
 @NotNull @DecimalMin("0") BigDecimal minimumOrder, boolean active) {}
