package pe.laherradura.entity;
import jakarta.persistence.*;
import pe.laherradura.enums.ProductUnit;
import java.math.BigDecimal;
@Entity
@Table(name="products")
public class Product {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="category_id") private Category category;
 @Column(nullable=false,unique=true,length=150) private String name;
 @Column(length=800) private String description;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal pricePerUnit;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private ProductUnit unit=ProductUnit.KG;
 @Column(nullable=false,precision=12,scale=3) private BigDecimal stockQuantity=BigDecimal.ZERO;
 @Column(nullable=false,precision=12,scale=3) private BigDecimal minimumQuantity=BigDecimal.valueOf(0.5);
 @Column(length=800) private String imageUrl;
 @Column(nullable=false) private boolean active=true;
 @Column(nullable=false) private boolean featured=false;
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public Category getCategory(){return category;} public void setCategory(Category v){category=v;}
 public String getName(){return name;} public void setName(String v){name=v;}
 public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public BigDecimal getPricePerUnit(){return pricePerUnit;} public void setPricePerUnit(BigDecimal v){pricePerUnit=v;}
 public ProductUnit getUnit(){return unit;} public void setUnit(ProductUnit v){unit=v;}
 public BigDecimal getStockQuantity(){return stockQuantity;} public void setStockQuantity(BigDecimal v){stockQuantity=v;}
 public BigDecimal getMinimumQuantity(){return minimumQuantity;} public void setMinimumQuantity(BigDecimal v){minimumQuantity=v;}
 public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
 public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
 public boolean isFeatured(){return featured;} public void setFeatured(boolean v){featured=v;}
}
