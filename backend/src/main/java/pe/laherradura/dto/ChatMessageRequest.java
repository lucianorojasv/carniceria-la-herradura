package pe.laherradura.dto;
import jakarta.validation.constraints.NotBlank;
public record ChatMessageRequest(@NotBlank String phone, String customerName, @NotBlank String message) {}
