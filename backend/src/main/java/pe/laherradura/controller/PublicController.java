package pe.laherradura.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.laherradura.dto.OrderCreateRequest;
import pe.laherradura.dto.PublicOrderResponse;
import pe.laherradura.dto.StoreLocationResponse;
import pe.laherradura.entity.BusinessSetting;
import pe.laherradura.service.BusinessHoursService;
import pe.laherradura.service.CatalogService;
import pe.laherradura.service.DeliveryService;
import pe.laherradura.service.OrderService;
import pe.laherradura.service.PromotionService;
import pe.laherradura.service.SettingsService;
import pe.laherradura.service.StoreLocationService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final CatalogService catalog;
    private final PromotionService promotions;
    private final SettingsService settings;
    private final DeliveryService delivery;
    private final StoreLocationService storeLocations;
    private final BusinessHoursService businessHours;
    private final OrderService orders;

    public PublicController(CatalogService catalog,
                            PromotionService promotions,
                            SettingsService settings,
                            DeliveryService delivery,
                            StoreLocationService storeLocations,
                            BusinessHoursService businessHours,
                            OrderService orders) {
        this.catalog = catalog;
        this.promotions = promotions;
        this.settings = settings;
        this.delivery = delivery;
        this.storeLocations = storeLocations;
        this.businessHours = businessHours;
        this.orders = orders;
    }

    @GetMapping("/catalog")
    public Map<String, Object> catalog() {
        BusinessSetting business = settings.get();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("business", business);
        response.put("businessStatus", businessHours.status(business));
        response.put("categories", catalog.categories(true));
        response.put("products", catalog.products(true));
        response.put("featured", catalog.featured());
        response.put("promotions", promotions.list(true));
        response.put("deliveryZones", delivery.list(true));
        response.put("locations", storeLocations.list(true));
        response.put("mainLocation", storeLocations.mainPublic());
        return response;
    }

    @GetMapping("/store-locations")
    public List<StoreLocationResponse> storeLocations() {
        return storeLocations.list(true);
    }

    @GetMapping("/store-locations/{id}")
    public StoreLocationResponse storeLocation(@PathVariable Long id) {
        return storeLocations.get(id, true);
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public PublicOrderResponse createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return PublicOrderResponse.from(orders.createPublic(request));
    }

    @GetMapping("/orders/{code}")
    public PublicOrderResponse orderStatus(@PathVariable String code, @RequestParam String phone) {
        return PublicOrderResponse.from(orders.getPublic(code, phone));
    }
}
