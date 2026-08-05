package pe.laherradura.dto;
import jakarta.validation.constraints.NotBlank;
public record CategoryRequest(@NotBlank String name, String description, boolean active) {}
