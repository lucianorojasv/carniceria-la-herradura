package pe.laherradura.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.laherradura.dto.*;
import pe.laherradura.entity.*;
import pe.laherradura.exception.*;
import pe.laherradura.repository.*;
import java.util.List;
@Service
@Transactional
public class CatalogService {
 private final CategoryRepository categories; private final ProductRepository products;
 public CatalogService(CategoryRepository c,ProductRepository p){categories=c;products=p;}
 @Transactional(readOnly=true) public List<Category> categories(boolean onlyActive){return onlyActive?categories.findByActiveTrueOrderByNameAsc():categories.findAll();}
 public Category saveCategory(Long id,CategoryRequest r){Category c=id==null?new Category():categories.findById(id).orElseThrow(()->new NotFoundException("Categoría no encontrada"));c.setName(r.name().trim());c.setDescription(r.description());c.setActive(r.active());return categories.save(c);}
 @Transactional(readOnly=true) public List<Product> products(boolean onlyActive){return onlyActive?products.findByActiveTrueOrderByNameAsc():products.findAll();}
 @Transactional(readOnly=true) public List<Product> byCategory(Long categoryId){return products.findByCategoryIdAndActiveTrueOrderByNameAsc(categoryId);}
 @Transactional(readOnly=true) public List<Product> featured(){return products.findByFeaturedTrueAndActiveTrueOrderByNameAsc();}
 public Product saveProduct(Long id,ProductRequest r){
   Product p=id==null?new Product():products.findById(id).orElseThrow(()->new NotFoundException("Producto no encontrado"));
   Category c=categories.findById(r.categoryId()).orElseThrow(()->new NotFoundException("Categoría no encontrada"));
   p.setCategory(c);p.setName(r.name().trim());p.setDescription(r.description());p.setPricePerUnit(r.pricePerUnit());p.setUnit(r.unit());p.setStockQuantity(r.stockQuantity());p.setMinimumQuantity(r.minimumQuantity());p.setImageUrl(r.imageUrl());p.setActive(r.active());p.setFeatured(r.featured());
   return products.save(p);
 }
 public void deleteProduct(Long id){Product p=products.findById(id).orElseThrow(()->new NotFoundException("Producto no encontrado"));p.setActive(false);products.save(p);}
}
