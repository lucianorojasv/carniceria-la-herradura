package pe.laherradura.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "business_settings")
public class BusinessSetting {
    @Id private Long id = 1L;
    @Column(nullable=false, length=160) private String businessName = "Carnicería La Herradura";
    @Column(length=30) private String phone = "938149352";
    @Column(length=300) private String address = "[PENDIENTE]";
    @Column(length=300) private String openingHours = "[PENDIENTE]";
    @Column(nullable=false) private boolean deliveryEnabled = true;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal minimumDeliveryAmount = BigDecimal.valueOf(50);
    @Column(nullable=false, length=8) private String currency = "PEN";
    @Column(length=500) private String welcomeMessage = "Calidad, frescura y sabor para tu mesa";
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getBusinessName(){return businessName;} public void setBusinessName(String v){businessName=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public String getAddress(){return address;} public void setAddress(String v){address=v;}
    public String getOpeningHours(){return openingHours;} public void setOpeningHours(String v){openingHours=v;}
    public boolean isDeliveryEnabled(){return deliveryEnabled;} public void setDeliveryEnabled(boolean v){deliveryEnabled=v;}
    public BigDecimal getMinimumDeliveryAmount(){return minimumDeliveryAmount;} public void setMinimumDeliveryAmount(BigDecimal v){minimumDeliveryAmount=v;}
    public String getCurrency(){return currency;} public void setCurrency(String v){currency=v;}
    public String getWelcomeMessage(){return welcomeMessage;} public void setWelcomeMessage(String v){welcomeMessage=v;}
}
