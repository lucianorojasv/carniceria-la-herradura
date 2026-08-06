package pe.laherradura.dto;

import java.math.BigDecimal;
import java.util.List;

public record StoreLocationResponse(
        Long id,
        String name,
        String address,
        String district,
        String province,
        String department,
        BigDecimal latitude,
        BigDecimal longitude,
        String googlePlaceId,
        String googleMapsUrl,
        String googleMapsEmbedUrl,
        String directionsUrl,
        String phone,
        String whatsappNumber,
        String referenceText,
        String parkingInformation,
        boolean main,
        boolean active,
        boolean openNow,
        String todaySchedule,
        List<Image> images
) {
    public record Image(
            Long id,
            String imageUrl,
            String title,
            String description,
            String altText,
            String imageType,
            int displayOrder,
            boolean visible
    ) { }
}
