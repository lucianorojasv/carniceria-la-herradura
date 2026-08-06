package pe.laherradura.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.laherradura.dto.StoreLocationImageRequest;
import pe.laherradura.dto.StoreLocationRequest;
import pe.laherradura.dto.StoreLocationResponse;
import pe.laherradura.service.StoreLocationService;

import java.util.List;

@RestController
@RequestMapping("/api/store-locations")
@PreAuthorize("hasRole('ADMIN')")
public class StoreLocationController {
    private final StoreLocationService service;

    public StoreLocationController(StoreLocationService service) {
        this.service = service;
    }

    @GetMapping
    public List<StoreLocationResponse> list() {
        return service.list(false);
    }

    @GetMapping("/{id}")
    public StoreLocationResponse get(@PathVariable Long id) {
        return service.get(id, false);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoreLocationResponse create(@Valid @RequestBody StoreLocationRequest request) {
        return service.save(null, request);
    }

    @PutMapping("/{id}")
    public StoreLocationResponse update(@PathVariable Long id, @Valid @RequestBody StoreLocationRequest request) {
        return service.save(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deactivate(id);
    }

    @PostMapping(path = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public StoreLocationResponse.Image uploadImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "GALLERY") String imageType,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String altText) {
        return service.addImage(id, file, imageType, title, altText);
    }

    @PatchMapping("/{locationId}/images/{imageId}")
    public StoreLocationResponse.Image updateImage(
            @PathVariable Long locationId,
            @PathVariable Long imageId,
            @Valid @RequestBody StoreLocationImageRequest request) {
        return service.updateImage(locationId, imageId, request);
    }

    @DeleteMapping("/{locationId}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@PathVariable Long locationId, @PathVariable Long imageId) {
        service.deleteImage(locationId, imageId);
    }
}
