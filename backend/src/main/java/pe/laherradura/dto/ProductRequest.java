package pe.laherradura.dto;
import jakarta.validation.constraints.*;
import pe.laherradura.enums.ProductUnit;
import java.math.BigDecimal;
public record ProductRequest(
 @NotNull Long categoryId,
 @NotBlank String name,
 String description,
 @NotNull @DecimalMin("0.01") BigDecimal pricePerUnit,
 @NotNull ProductUnit unit,
 @NotNull @DecimalMin("0.0") BigDecimal stockQuantity,
 @NotNull @DecimalMin("0.01") BigDecimal minimumQuantity,
 String imageUrl,
 boolean active,
 boolean featured
) {}
