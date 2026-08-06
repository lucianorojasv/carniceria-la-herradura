package pe.laherradura.service;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import pe.laherradura.exception.BusinessException;

@Service
public class MediaStorageService {

    private static final long MAX_IMAGE_SIZE = 4L * 1024L * 1024L;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp");

    private final String supabaseUrl;
    private final String adminApiKey;
    private final String bucket;
    private final RestClient restClient;

    public MediaStorageService(
            @Value("${SUPABASE_URL:}") String supabaseUrl,
            @Value("${SUPABASE_SECRET_KEY:${SUPABASE_SERVICE_ROLE_KEY:}}") String adminApiKey,
            @Value("${SUPABASE_STORAGE_BUCKET:product-images}") String bucket) {
        this.supabaseUrl = stripTrailingSlash(supabaseUrl);
        this.adminApiKey = adminApiKey == null ? "" : adminApiKey.trim();
        this.bucket = StringUtils.hasText(bucket) ? bucket.trim() : "product-images";
        this.restClient = StringUtils.hasText(this.supabaseUrl)
                ? RestClient.builder().baseUrl(this.supabaseUrl).build()
                : null;
    }

    public Map<String, String> uploadProductImage(MultipartFile file) {
        return uploadImage(file, "products");
    }

    public Map<String, String> uploadPromotionImage(MultipartFile file) {
        return uploadImage(file, "promotions");
    }

    public Map<String, String> uploadPaymentQr(MultipartFile file) {
        return uploadImage(file, "payment-qr");
    }

    public Map<String, String> uploadStoreImage(MultipartFile file, Long locationId) {
        String locationFolder = locationId == null ? "unassigned" : String.valueOf(locationId);
        return uploadImage(file, "business-gallery/" + locationFolder);
    }

    private Map<String, String> uploadImage(MultipartFile file, String folder) {
        validateConfiguration();
        validateImage(file);

        String contentType = file.getContentType().toLowerCase(Locale.ROOT);
        String extension = extensionFor(contentType);
        String objectPath = folder + "/" + UUID.randomUUID() + extension;

        try {
            restClient.post()
                    .uri("/storage/v1/object/" + bucket + "/" + objectPath)
                    .header("apikey", adminApiKey)
                    .headers(headers -> addLegacyAuthorization(headers, adminApiKey))
                    .header("x-upsert", "false")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(file.getBytes())
                    .retrieve()
                    .toBodilessEntity();
        } catch (IOException exception) {
            throw new BusinessException("No se pudo leer la imagen seleccionada");
        } catch (Exception exception) {
            throw new BusinessException("No se pudo subir la imagen a Supabase Storage: "
                    + safeMessage(exception));
        }

        String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + objectPath;
        return Map.of("url", publicUrl, "path", objectPath);
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(supabaseUrl) || !StringUtils.hasText(adminApiKey)) {
            throw new BusinessException(
                    "Falta configurar SUPABASE_URL y SUPABASE_SECRET_KEY en Render");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Selecciona una imagen");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException("La imagen no debe superar los 4 MB");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)
                || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("Formato no permitido. Usa JPG, PNG o WEBP");
        }
    }

    private static void addLegacyAuthorization(HttpHeaders headers, String key) {
        // Las claves service_role antiguas son JWT. Las nuevas sb_secret_* se envían solo como apikey.
        if (key.startsWith("eyJ") && key.chars().filter(ch -> ch == '.').count() == 2) {
            headers.setBearerAuth(key);
        }
    }

    private static String extensionFor(String contentType) {
        return switch (contentType) {
            case MediaType.IMAGE_PNG_VALUE -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private static String stripTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) return "";
        return value.trim().replaceAll("/+$", "");
    }

    private static String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return StringUtils.hasText(message) ? message : exception.getClass().getSimpleName();
    }
}
