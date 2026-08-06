package pe.laherradura.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pe.laherradura.dto.StoreLocationImageRequest;
import pe.laherradura.dto.StoreLocationRequest;
import pe.laherradura.dto.StoreLocationResponse;
import pe.laherradura.entity.BusinessSetting;
import pe.laherradura.entity.StoreLocation;
import pe.laherradura.entity.StoreLocationImage;
import pe.laherradura.exception.BusinessException;
import pe.laherradura.exception.NotFoundException;
import pe.laherradura.repository.StoreLocationImageRepository;
import pe.laherradura.repository.StoreLocationRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional
public class StoreLocationService {
    private static final List<String> IMAGE_TYPES = List.of(
            "COVER", "FACADE", "INTERIOR", "COUNTER", "PRODUCT_DISPLAY", "PARKING", "REFERENCE", "GALLERY");

    private final StoreLocationRepository locations;
    private final StoreLocationImageRepository images;
    private final MediaStorageService mediaStorage;
    private final SettingsService settingsService;
    private final BusinessHoursService businessHours;

    public StoreLocationService(StoreLocationRepository locations,
                                StoreLocationImageRepository images,
                                MediaStorageService mediaStorage,
                                SettingsService settingsService,
                                BusinessHoursService businessHours) {
        this.locations = locations;
        this.images = images;
        this.mediaStorage = mediaStorage;
        this.settingsService = settingsService;
        this.businessHours = businessHours;
    }

    @Transactional(readOnly = true)
    public List<StoreLocationResponse> list(boolean onlyActive) {
        return (onlyActive
                ? locations.findByActiveTrueOrderByMainDescNameAsc()
                : locations.findAllByOrderByMainDescNameAsc())
                .stream().map(location -> toResponse(location, onlyActive)).toList();
    }

    @Transactional(readOnly = true)
    public StoreLocationResponse get(Long id, boolean publicView) {
        StoreLocation location = locations.findById(id)
                .orElseThrow(() -> new NotFoundException("Local no encontrado"));
        if (publicView && !location.isActive()) throw new NotFoundException("Local no encontrado");
        return toResponse(location, publicView);
    }

    @Transactional(readOnly = true)
    public StoreLocationResponse mainPublic() {
        return locations.findFirstByMainTrueAndActiveTrue()
                .map(location -> toResponse(location, true))
                .orElseGet(() -> locations.findByActiveTrueOrderByMainDescNameAsc().stream()
                        .findFirst().map(location -> toResponse(location, true)).orElse(null));
    }

    public StoreLocationResponse save(Long id, StoreLocationRequest request) {
        StoreLocation location = id == null
                ? new StoreLocation()
                : locations.findById(id).orElseThrow(() -> new NotFoundException("Local no encontrado"));

        if (request.main()) {
            List<StoreLocation> previousMains = locations.findAll().stream()
                    .filter(StoreLocation::isMain)
                    .filter(other -> location.getId() == null || !other.getId().equals(location.getId()))
                    .peek(other -> other.setMain(false))
                    .toList();
            if (!previousMains.isEmpty()) {
                locations.saveAll(previousMains);
                locations.flush();
            }
        }

        location.setName(clean(request.name()));
        location.setAddress(clean(request.address()));
        location.setDistrict(cleanNullable(request.district()));
        location.setProvince(cleanNullable(request.province()));
        location.setDepartment(cleanNullable(request.department()));
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        location.setGooglePlaceId(cleanNullable(request.googlePlaceId()));
        location.setGoogleMapsUrl(validateExternalUrl(request.googleMapsUrl(), "enlace de Google Maps"));
        location.setGoogleMapsEmbedUrl(validateEmbedUrl(request.googleMapsEmbedUrl()));
        location.setPhone(cleanNullable(request.phone()));
        location.setWhatsappNumber(cleanNullable(request.whatsappNumber()));
        location.setReferenceText(cleanNullable(request.referenceText()));
        location.setParkingInformation(cleanNullable(request.parkingInformation()));
        location.setMain(request.main() && request.active());
        location.setActive(request.active());

        StoreLocation saved = locations.save(location);
        return toResponse(saved, false);
    }

    public void deactivate(Long id) {
        StoreLocation location = locations.findById(id)
                .orElseThrow(() -> new NotFoundException("Local no encontrado"));
        location.setActive(false);
        location.setMain(false);
        locations.save(location);
    }

    public StoreLocationResponse.Image addImage(Long locationId,
                                                MultipartFile file,
                                                String imageType,
                                                String title,
                                                String altText) {
        StoreLocation location = locations.findById(locationId)
                .orElseThrow(() -> new NotFoundException("Local no encontrado"));
        if (location.getImages().size() >= 24) {
            throw new BusinessException("Cada local puede tener como máximo 24 fotografías");
        }
        Map<String, String> upload = mediaStorage.uploadStoreImage(file, locationId);
        StoreLocationImage image = new StoreLocationImage();
        image.setStoreLocation(location);
        image.setImageUrl(upload.get("url"));
        image.setImageType(normalizeImageType(imageType));
        image.setTitle(cleanNullable(title));
        image.setAltText(StringUtils.hasText(altText) ? altText.trim() : defaultAlt(location, image.getImageType()));
        int nextOrder = location.getImages().stream().mapToInt(StoreLocationImage::getDisplayOrder).max().orElse(-1) + 1;
        image.setDisplayOrder(nextOrder);
        image.setVisible(true);
        return toImageResponse(images.save(image));
    }

    public StoreLocationResponse.Image updateImage(Long locationId, Long imageId, StoreLocationImageRequest request) {
        StoreLocationImage image = image(imageId, locationId);
        image.setTitle(cleanNullable(request.title()));
        image.setDescription(cleanNullable(request.description()));
        image.setAltText(cleanNullable(request.altText()));
        image.setImageType(normalizeImageType(request.imageType()));
        if (request.displayOrder() != null) image.setDisplayOrder(request.displayOrder());
        if (request.visible() != null) image.setVisible(request.visible());
        return toImageResponse(images.save(image));
    }

    public void deleteImage(Long locationId, Long imageId) {
        images.delete(image(imageId, locationId));
    }

    private StoreLocationImage image(Long imageId, Long locationId) {
        StoreLocationImage image = images.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Fotografía no encontrada"));
        if (!image.getStoreLocation().getId().equals(locationId)) {
            throw new NotFoundException("Fotografía no encontrada");
        }
        return image;
    }

    private StoreLocationResponse toResponse(StoreLocation location, boolean publicView) {
        BusinessSetting settings = settingsService.get();
        BusinessHoursService.Status status = businessHours.status(settings);
        List<StoreLocationResponse.Image> locationImages = location.getImages().stream()
                .filter(image -> !publicView || image.isVisible())
                .sorted(Comparator.comparingInt(StoreLocationImage::getDisplayOrder)
                        .thenComparing(StoreLocationImage::getId))
                .map(this::toImageResponse)
                .toList();
        return new StoreLocationResponse(
                location.getId(), location.getName(), location.getAddress(), location.getDistrict(),
                location.getProvince(), location.getDepartment(), location.getLatitude(), location.getLongitude(),
                location.getGooglePlaceId(), location.getGoogleMapsUrl(), location.getGoogleMapsEmbedUrl(),
                directionsUrl(location), location.getPhone(), location.getWhatsappNumber(),
                location.getReferenceText(), location.getParkingInformation(), location.isMain(), location.isActive(),
                status.open(), businessHours.scheduleDescription(settings), locationImages
        );
    }

    private StoreLocationResponse.Image toImageResponse(StoreLocationImage image) {
        return new StoreLocationResponse.Image(
                image.getId(), image.getImageUrl(), image.getTitle(), image.getDescription(), image.getAltText(),
                image.getImageType(), image.getDisplayOrder(), image.isVisible());
    }

    private String directionsUrl(StoreLocation location) {
        String destination;
        if (location.getLatitude() != null && location.getLongitude() != null) {
            destination = location.getLatitude().toPlainString() + "," + location.getLongitude().toPlainString();
        } else {
            destination = String.join(", ", List.of(
                    safe(location.getAddress()), safe(location.getDistrict()), safe(location.getProvince()), safe(location.getDepartment())))
                    .replaceAll("(^,\\s*)|(,\\s*,)+|(,\\s*$)", "");
        }
        StringBuilder url = new StringBuilder("https://www.google.com/maps/dir/?api=1&destination=")
                .append(encode(destination));
        if (StringUtils.hasText(location.getGooglePlaceId())) {
            url.append("&destination_place_id=").append(encode(location.getGooglePlaceId()));
        }
        url.append("&travelmode=driving");
        return url.toString();
    }

    private String validateEmbedUrl(String value) {
        String url = cleanNullable(value);
        if (url == null) return null;
        if (!(url.startsWith("https://www.google.com/maps/embed")
                || url.startsWith("https://maps.google.com/maps"))) {
            throw new BusinessException("La URL del mapa insertado debe pertenecer a Google Maps");
        }
        return url;
    }

    private String validateExternalUrl(String value, String fieldName) {
        String url = cleanNullable(value);
        if (url == null) return null;
        if (!(url.startsWith("https://www.google.com/maps/") || url.startsWith("https://maps.app.goo.gl/"))) {
            throw new BusinessException("El " + fieldName + " no es válido");
        }
        return url;
    }

    private String normalizeImageType(String value) {
        String type = StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : "GALLERY";
        return IMAGE_TYPES.contains(type) ? type : "GALLERY";
    }

    private String defaultAlt(StoreLocation location, String type) {
        return switch (type) {
            case "FACADE", "COVER" -> "Fachada de " + location.getName();
            case "INTERIOR" -> "Interior de " + location.getName();
            case "COUNTER" -> "Mostrador de atención de " + location.getName();
            case "PARKING" -> "Zona de estacionamiento de " + location.getName();
            default -> "Fotografía de " + location.getName();
        };
    }

    private String clean(String value) { return value.trim(); }
    private String cleanNullable(String value) { return StringUtils.hasText(value) ? value.trim() : null; }
    private String safe(String value) { return value == null ? "" : value.trim(); }
    private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
}
