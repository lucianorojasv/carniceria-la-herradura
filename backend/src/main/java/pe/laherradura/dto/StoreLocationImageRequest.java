package pe.laherradura.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record StoreLocationImageRequest(
        @Size(max = 150) String title,
        @Size(max = 300) String description,
        @Size(max = 200) String altText,
        @Size(max = 30) String imageType,
        @Min(0) Integer displayOrder,
        Boolean visible
) { }
