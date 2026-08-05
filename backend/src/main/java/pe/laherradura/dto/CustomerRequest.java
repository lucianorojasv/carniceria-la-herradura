package pe.laherradura.dto;
import jakarta.validation.constraints.NotBlank;
public record CustomerRequest(@NotBlank String name, @NotBlank String phone, String address,
 String reference, String zone, boolean consentMarketing) {}
