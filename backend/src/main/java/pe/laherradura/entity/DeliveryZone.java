package pe.laherradura.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity
@Table(name="delivery_zones")
public class DeliveryZone {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=120) private String name;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal fee=BigDecimal.ZERO;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal minimumOrder=BigDecimal.ZERO;
 @Column(nullable=false) private boolean active=true;
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public String getName(){return name;} public void setName(String v){name=v;}
 public BigDecimal getFee(){return fee;} public void setFee(BigDecimal v){fee=v;}
 public BigDecimal getMinimumOrder(){return minimumOrder;} public void setMinimumOrder(BigDecimal v){minimumOrder=v;}
 public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}
