package pe.laherradura.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity
@Table(name="order_items")
public class OrderItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @JsonIgnore @ManyToOne(optional=false) @JoinColumn(name="order_id") private CustomerOrder order;
 @ManyToOne(optional=false) @JoinColumn(name="product_id") private Product product;
 @Column(nullable=false,length=150) private String productName;
 @Column(nullable=false,precision=12,scale=3) private BigDecimal quantity;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal unitPrice;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal subtotal;
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public CustomerOrder getOrder(){return order;} public void setOrder(CustomerOrder v){order=v;}
 public Product getProduct(){return product;} public void setProduct(Product v){product=v;}
 public String getProductName(){return productName;} public void setProductName(String v){productName=v;}
 public BigDecimal getQuantity(){return quantity;} public void setQuantity(BigDecimal v){quantity=v;}
 public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal v){unitPrice=v;}
 public BigDecimal getSubtotal(){return subtotal;} public void setSubtotal(BigDecimal v){subtotal=v;}
}
