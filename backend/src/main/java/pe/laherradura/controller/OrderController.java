package pe.laherradura.controller;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.laherradura.dto.*;
import pe.laherradura.entity.CustomerOrder;
import pe.laherradura.service.OrderService;
import java.util.List;
@RestController @RequestMapping("/api/orders")
public class OrderController {private final OrderService s;public OrderController(OrderService s){this.s=s;}@GetMapping public List<CustomerOrder> list(){return s.list();}@GetMapping("/{id}") public CustomerOrder get(@PathVariable Long id){return s.get(id);}@PostMapping @ResponseStatus(HttpStatus.CREATED) public CustomerOrder create(@Valid @RequestBody OrderCreateRequest r){return s.create(r);}@PatchMapping("/{id}/status") public CustomerOrder status(@PathVariable Long id,@Valid @RequestBody OrderStatusRequest r){return s.updateStatus(id,r.status());}}
