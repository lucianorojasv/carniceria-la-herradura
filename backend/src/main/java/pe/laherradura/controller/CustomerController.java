package pe.laherradura.controller;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.laherradura.dto.CustomerRequest;
import pe.laherradura.entity.Customer;
import pe.laherradura.service.CustomerService;
import java.util.List;
@RestController @RequestMapping("/api/customers")
public class CustomerController {private final CustomerService s;public CustomerController(CustomerService s){this.s=s;}@GetMapping public List<Customer> list(){return s.list();}@PostMapping @ResponseStatus(HttpStatus.CREATED) public Customer create(@Valid @RequestBody CustomerRequest r){return s.save(null,r);}@PutMapping("/{id}") public Customer update(@PathVariable Long id,@Valid @RequestBody CustomerRequest r){return s.save(id,r);}}
