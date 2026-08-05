package pe.laherradura.entity;
import jakarta.persistence.*;
import pe.laherradura.enums.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name="customer_orders")
public class CustomerOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=40) private String code;
 @ManyToOne(optional=false) @JoinColumn(name="customer_id") private Customer customer;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private OrderStatus status=OrderStatus.PENDING;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private FulfillmentType fulfillmentType=FulfillmentType.PICKUP;
 @ManyToOne @JoinColumn(name="delivery_zone_id") private DeliveryZone deliveryZone;
 @Column(length=500) private String deliveryAddress;
 @Column(length=500) private String deliveryReference;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private PaymentMethod paymentMethod=PaymentMethod.CASH;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private OrderSource source=OrderSource.WHATSAPP;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal subtotal=BigDecimal.ZERO;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal deliveryFee=BigDecimal.ZERO;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal total=BigDecimal.ZERO;
 @Column(length=1000) private String notes;
 @Column(nullable=false) private OffsetDateTime createdAt=OffsetDateTime.now();
 @Column(nullable=false) private OffsetDateTime updatedAt=OffsetDateTime.now();
 @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.EAGER)
 private List<OrderItem> items=new ArrayList<>();
 @PreUpdate void preUpdate(){updatedAt=OffsetDateTime.now();}
 public void addItem(OrderItem item){items.add(item);item.setOrder(this);}
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public String getCode(){return code;} public void setCode(String v){code=v;}
 public Customer getCustomer(){return customer;} public void setCustomer(Customer v){customer=v;}
 public OrderStatus getStatus(){return status;} public void setStatus(OrderStatus v){status=v;}
 public FulfillmentType getFulfillmentType(){return fulfillmentType;} public void setFulfillmentType(FulfillmentType v){fulfillmentType=v;}
 public DeliveryZone getDeliveryZone(){return deliveryZone;} public void setDeliveryZone(DeliveryZone v){deliveryZone=v;}
 public String getDeliveryAddress(){return deliveryAddress;} public void setDeliveryAddress(String v){deliveryAddress=v;}
 public String getDeliveryReference(){return deliveryReference;} public void setDeliveryReference(String v){deliveryReference=v;}
 public PaymentMethod getPaymentMethod(){return paymentMethod;} public void setPaymentMethod(PaymentMethod v){paymentMethod=v;}
 public OrderSource getSource(){return source;} public void setSource(OrderSource v){source=v;}
 public BigDecimal getSubtotal(){return subtotal;} public void setSubtotal(BigDecimal v){subtotal=v;}
 public BigDecimal getDeliveryFee(){return deliveryFee;} public void setDeliveryFee(BigDecimal v){deliveryFee=v;}
 public BigDecimal getTotal(){return total;} public void setTotal(BigDecimal v){total=v;}
 public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
 public OffsetDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(OffsetDateTime v){createdAt=v;}
 public OffsetDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(OffsetDateTime v){updatedAt=v;}
 public List<OrderItem> getItems(){return items;} public void setItems(List<OrderItem> v){items=v;}
}
