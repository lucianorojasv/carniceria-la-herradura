package pe.laherradura.entity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
@Entity
@Table(name="customers")
public class Customer {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=150) private String name;
 @Column(nullable=false,unique=true,length=30) private String phone;
 @Column(length=400) private String address;
 @Column(length=400) private String reference;
 @Column(length=120) private String zone;
 @Column(nullable=false) private boolean consentMarketing=false;
 @Column(nullable=false) private OffsetDateTime createdAt=OffsetDateTime.now();
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public String getName(){return name;} public void setName(String v){name=v;}
 public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
 public String getAddress(){return address;} public void setAddress(String v){address=v;}
 public String getReference(){return reference;} public void setReference(String v){reference=v;}
 public String getZone(){return zone;} public void setZone(String v){zone=v;}
 public boolean isConsentMarketing(){return consentMarketing;} public void setConsentMarketing(boolean v){consentMarketing=v;}
 public OffsetDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(OffsetDateTime v){createdAt=v;}
}
