package pe.laherradura.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.laherradura.dto.*;
import pe.laherradura.entity.*;
import pe.laherradura.enums.*;
import pe.laherradura.exception.*;
import pe.laherradura.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
@Service @Transactional
public class OrderService {
 private final CustomerOrderRepository orders; private final ProductRepository products; private final CustomerService customers; private final DeliveryZoneRepository zones;
 public OrderService(CustomerOrderRepository o,ProductRepository p,CustomerService c,DeliveryZoneRepository z){orders=o;products=p;customers=c;zones=z;}
 @Transactional(readOnly=true) public List<CustomerOrder> list(){return orders.findTop100ByOrderByCreatedAtDesc();}
 @Transactional(readOnly=true) public CustomerOrder get(Long id){return orders.findById(id).orElseThrow(()->new NotFoundException("Pedido no encontrado"));}
 public CustomerOrder create(OrderCreateRequest r){
   Customer customer=customers.getOrCreate(r.customerPhone(),r.customerName());
   if(r.customerName()!=null&&!r.customerName().isBlank())customer.setName(r.customerName().trim());
   if(r.deliveryAddress()!=null&&!r.deliveryAddress().isBlank())customer.setAddress(r.deliveryAddress());
   if(r.deliveryReference()!=null)customer.setReference(r.deliveryReference());
   CustomerOrder order=new CustomerOrder();order.setCode(code());order.setCustomer(customer);
   order.setFulfillmentType(r.fulfillmentType()==null?FulfillmentType.PICKUP:r.fulfillmentType());
   order.setPaymentMethod(r.paymentMethod()==null?PaymentMethod.CASH:r.paymentMethod());
   order.setSource(r.source()==null?OrderSource.WHATSAPP:r.source());order.setNotes(r.notes());
   BigDecimal subtotal=BigDecimal.ZERO;
   for(OrderCreateRequest.Item i:r.items()){
     Product p=products.findByIdForUpdate(i.productId()).orElseThrow(()->new NotFoundException("Producto no encontrado: "+i.productId()));
     if(!p.isActive())throw new BusinessException(p.getName()+" no está disponible");
     if(i.quantity().compareTo(p.getMinimumQuantity())<0)throw new BusinessException("Cantidad mínima para "+p.getName()+": "+p.getMinimumQuantity());
     if(p.getStockQuantity().compareTo(i.quantity())<0)throw new BusinessException("Stock insuficiente de "+p.getName());
     BigDecimal line=p.getPricePerUnit().multiply(i.quantity()).setScale(2,RoundingMode.HALF_UP);
     OrderItem item=new OrderItem();item.setProduct(p);item.setProductName(p.getName());item.setQuantity(i.quantity());item.setUnitPrice(p.getPricePerUnit());item.setSubtotal(line);order.addItem(item);
     subtotal=subtotal.add(line);p.setStockQuantity(p.getStockQuantity().subtract(i.quantity()));products.save(p);
   }
   BigDecimal fee=BigDecimal.ZERO;
   if(order.getFulfillmentType()==FulfillmentType.DELIVERY){
     if(r.deliveryZoneId()==null)throw new BusinessException("Seleccione una zona de delivery");
     DeliveryZone zone=zones.findById(r.deliveryZoneId()).orElseThrow(()->new NotFoundException("Zona de delivery no encontrada"));
     if(!zone.isActive())throw new BusinessException("Zona de delivery no disponible");
     if(subtotal.compareTo(zone.getMinimumOrder())<0)throw new BusinessException("Pedido mínimo para "+zone.getName()+": S/ "+zone.getMinimumOrder());
     order.setDeliveryZone(zone);fee=zone.getFee();order.setDeliveryAddress(r.deliveryAddress());order.setDeliveryReference(r.deliveryReference());customer.setZone(zone.getName());
   }
   order.setSubtotal(subtotal);order.setDeliveryFee(fee);order.setTotal(subtotal.add(fee));order.setStatus(OrderStatus.PENDING);
   return orders.save(order);
 }
 public CustomerOrder updateStatus(Long id,OrderStatus status){
   CustomerOrder o=get(id);OrderStatus old=o.getStatus();
   if(old==OrderStatus.CANCELLED && status!=OrderStatus.CANCELLED)throw new BusinessException("Un pedido cancelado no puede reabrirse automáticamente");
   if(status==OrderStatus.CANCELLED && old!=OrderStatus.CANCELLED){for(OrderItem i:o.getItems()){Product p=i.getProduct();p.setStockQuantity(p.getStockQuantity().add(i.getQuantity()));products.save(p);}}
   o.setStatus(status);return orders.save(o);
 }
 private String code(){return "LH-"+LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)+"-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();}
}
