package pe.laherradura.controller;
import org.springframework.web.bind.annotation.*;
import pe.laherradura.entity.*;
import pe.laherradura.service.*;
import java.util.*;
@RestController @RequestMapping("/api/public")
public class PublicController {
 private final CatalogService catalog;private final PromotionService promotions;private final SettingsService settings;private final DeliveryService delivery;
 public PublicController(CatalogService c,PromotionService p,SettingsService s,DeliveryService d){catalog=c;promotions=p;settings=s;delivery=d;}
 @GetMapping("/catalog") public Map<String,Object> catalog(){return Map.of("business",settings.get(),"categories",catalog.categories(true),"products",catalog.products(true),"featured",catalog.featured(),"promotions",promotions.list(true),"deliveryZones",delivery.list(true));}
}
