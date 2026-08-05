package pe.laherradura.controller;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.laherradura.dto.DeliveryZoneRequest;
import pe.laherradura.entity.DeliveryZone;
import pe.laherradura.service.DeliveryService;
import java.util.List;
@RestController @RequestMapping("/api/delivery-zones")
public class DeliveryController {private final DeliveryService s;public DeliveryController(DeliveryService s){this.s=s;}@GetMapping public List<DeliveryZone> list(){return s.list(false);}@PostMapping @ResponseStatus(HttpStatus.CREATED) public DeliveryZone create(@Valid @RequestBody DeliveryZoneRequest r){return s.save(null,r);}@PutMapping("/{id}") public DeliveryZone update(@PathVariable Long id,@Valid @RequestBody DeliveryZoneRequest r){return s.save(id,r);}}
