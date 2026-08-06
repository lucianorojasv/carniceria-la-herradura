package pe.laherradura.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record StoreLocationRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 300) String address,
        @Size(max = 100) String district,
        @Size(max = 100) String province,
        @Size(max = 100) String department,
        @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
        @Size(max = 255) String googlePlaceId,
        @Size(max = 1000) String googleMapsUrl,
        @Size(max = 1200) String googleMapsEmbedUrl,
        @Size(max = 30) String phone,
        @Size(max = 30) String whatsappNumber,
        @Size(max = 300) String referenceText,
        @Size(max = 300) String parkingInformation,
        boolean main,
        boolean active
) { }
