package pe.laherradura.controller;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.laherradura.dto.*;
import pe.laherradura.entity.*;
import pe.laherradura.service.CatalogService;
import java.util.List;
@RestController @RequestMapping("/api")
public class CatalogController {
 private final CatalogService service;public CatalogController(CatalogService s){service=s;}
 @GetMapping("/categories") public List<Category> categories(){return service.categories(false);}
 @PostMapping("/categories") @ResponseStatus(HttpStatus.CREATED) public Category createCategory(@Valid @RequestBody CategoryRequest r){return service.saveCategory(null,r);}
 @PutMapping("/categories/{id}") public Category updateCategory(@PathVariable Long id,@Valid @RequestBody CategoryRequest r){return service.saveCategory(id,r);}
 @GetMapping("/products") public List<Product> products(){return service.products(false);}
 @PostMapping("/products") @ResponseStatus(HttpStatus.CREATED) public Product createProduct(@Valid @RequestBody ProductRequest r){return service.saveProduct(null,r);}
 @PutMapping("/products/{id}") public Product updateProduct(@PathVariable Long id,@Valid @RequestBody ProductRequest r){return service.saveProduct(id,r);}
 @DeleteMapping("/products/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id){service.deleteProduct(id);}
}
