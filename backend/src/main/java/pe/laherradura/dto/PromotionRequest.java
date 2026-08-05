package pe.laherradura.dto;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
public record PromotionRequest(@NotBlank String name, String description, BigDecimal promotionalPrice,
 LocalDate startDate, LocalDate endDate, boolean active, String imageUrl) {}
