package pe.laherradura.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import pe.laherradura.service.MediaStorageService;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaStorageService storageService;

    public MediaController(MediaStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(path = "/product-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> uploadProductImage(@RequestPart("file") MultipartFile file) {
        return storageService.uploadProductImage(file);
    }

    @PostMapping(path = "/promotion-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> uploadPromotionImage(@RequestPart("file") MultipartFile file) {
        return storageService.uploadPromotionImage(file);
    }
}
