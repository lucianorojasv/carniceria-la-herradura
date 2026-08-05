package pe.laherradura.controller;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.laherradura.dto.PromotionRequest;
import pe.laherradura.entity.Promotion;
import pe.laherradura.service.PromotionService;
import java.util.List;
@RestController @RequestMapping("/api/promotions")
public class PromotionController {private final PromotionService s;public PromotionController(PromotionService s){this.s=s;}@GetMapping public List<Promotion> list(){return s.list(false);}@PostMapping @ResponseStatus(HttpStatus.CREATED) public Promotion create(@Valid @RequestBody PromotionRequest r){return s.save(null,r);}@PutMapping("/{id}") public Promotion update(@PathVariable Long id,@Valid @RequestBody PromotionRequest r){return s.save(id,r);}}
