package pe.laherradura.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity
@Table(name="promotions")
public class Promotion {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=160) private String name;
 @Column(length=800) private String description;
 @Column(precision=12,scale=2) private BigDecimal promotionalPrice;
 private LocalDate startDate; private LocalDate endDate;
 @Column(nullable=false) private boolean active=true;
 @Column(length=800) private String imageUrl;
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public String getName(){return name;} public void setName(String v){name=v;}
 public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public BigDecimal getPromotionalPrice(){return promotionalPrice;} public void setPromotionalPrice(BigDecimal v){promotionalPrice=v;}
 public LocalDate getStartDate(){return startDate;} public void setStartDate(LocalDate v){startDate=v;}
 public LocalDate getEndDate(){return endDate;} public void setEndDate(LocalDate v){endDate=v;}
 public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
 public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
}
