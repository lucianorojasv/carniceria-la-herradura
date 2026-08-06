package pe.laherradura.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.laherradura.dto.OrderCreateRequest;
import pe.laherradura.entity.Customer;
import pe.laherradura.entity.CustomerOrder;
import pe.laherradura.entity.DeliveryZone;
import pe.laherradura.entity.OrderItem;
import pe.laherradura.entity.Product;
import pe.laherradura.enums.FulfillmentType;
import pe.laherradura.enums.OrderSource;
import pe.laherradura.enums.OrderStatus;
import pe.laherradura.enums.PaymentMethod;
import pe.laherradura.exception.BusinessException;
import pe.laherradura.exception.NotFoundException;
import pe.laherradura.repository.CustomerOrderRepository;
import pe.laherradura.repository.DeliveryZoneRepository;
import pe.laherradura.repository.ProductRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {
    private final CustomerOrderRepository orders;
    private final ProductRepository products;
    private final CustomerService customers;
    private final DeliveryZoneRepository zones;

    public OrderService(CustomerOrderRepository orders, ProductRepository products,
                        CustomerService customers, DeliveryZoneRepository zones) {
        this.orders = orders;
        this.products = products;
        this.customers = customers;
        this.zones = zones;
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> list() {
        return orders.findTop100ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public CustomerOrder get(Long id) {
        return orders.findById(id).orElseThrow(() -> new NotFoundException("Pedido no encontrado"));
    }

    public CustomerOrder create(OrderCreateRequest request) {
        Customer customer = customers.getOrCreate(request.customerPhone(), request.customerName());
        if (request.customerName() != null && !request.customerName().isBlank()) customer.setName(request.customerName().trim());
        if (request.deliveryAddress() != null && !request.deliveryAddress().isBlank()) customer.setAddress(request.deliveryAddress());
        if (request.deliveryReference() != null) customer.setReference(request.deliveryReference());

        CustomerOrder order = new CustomerOrder();
        order.setCode(code());
        order.setCustomer(customer);
        order.setFulfillmentType(request.fulfillmentType() == null ? FulfillmentType.PICKUP : request.fulfillmentType());
        order.setPaymentMethod(request.paymentMethod() == null ? PaymentMethod.CASH : request.paymentMethod());
        order.setSource(request.source() == null ? OrderSource.WHATSAPP : request.source());
        order.setNotes(request.notes());
        order.setScheduledFor(request.scheduledFor());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderCreateRequest.Item requestedItem : request.items()) {
            Product product = products.findByIdForUpdate(requestedItem.productId())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + requestedItem.productId()));
            if (!product.isActive()) throw new BusinessException(product.getName() + " no está disponible");
            if (requestedItem.quantity().compareTo(product.getMinimumQuantity()) < 0) {
                throw new BusinessException("Cantidad mínima para " + product.getName() + ": " + product.getMinimumQuantity());
            }
            if (product.getStockQuantity().compareTo(requestedItem.quantity()) < 0) {
                throw new BusinessException("Stock insuficiente de " + product.getName());
            }

            BigDecimal line = product.getPricePerUnit().multiply(requestedItem.quantity()).setScale(2, RoundingMode.HALF_UP);
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setQuantity(requestedItem.quantity());
            item.setUnitPrice(product.getPricePerUnit());
            item.setSubtotal(line);
            order.addItem(item);
            subtotal = subtotal.add(line);

            // Las reservas descuentan stock al confirmarse para evitar sobreventa.
            product.setStockQuantity(product.getStockQuantity().subtract(requestedItem.quantity()));
            products.save(product);
        }

        BigDecimal fee = BigDecimal.ZERO;
        if (order.getFulfillmentType() == FulfillmentType.DELIVERY) {
            if (request.deliveryZoneId() == null) throw new BusinessException("Seleccione una zona de delivery");
            DeliveryZone zone = zones.findById(request.deliveryZoneId())
                    .orElseThrow(() -> new NotFoundException("Zona de delivery no encontrada"));
            if (!zone.isActive()) throw new BusinessException("Zona de delivery no disponible");
            if (subtotal.compareTo(zone.getMinimumOrder()) < 0) {
                throw new BusinessException("Pedido mínimo para " + zone.getName() + ": S/ " + zone.getMinimumOrder());
            }
            order.setDeliveryZone(zone);
            fee = zone.getFee();
            order.setDeliveryAddress(request.deliveryAddress());
            order.setDeliveryReference(request.deliveryReference());
            customer.setZone(zone.getName());
        }

        order.setSubtotal(subtotal);
        order.setDeliveryFee(fee);
        order.setTotal(subtotal.add(fee));
        order.setStatus(OrderStatus.PENDING);
        return orders.save(order);
    }

    public CustomerOrder updateStatus(Long id, OrderStatus status) {
        CustomerOrder order = get(id);
        OrderStatus oldStatus = order.getStatus();
        if (oldStatus == OrderStatus.CANCELLED && status != OrderStatus.CANCELLED) {
            throw new BusinessException("Un pedido cancelado no puede reabrirse automáticamente");
        }
        if (status == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity().add(item.getQuantity()));
                products.save(product);
            }
        }
        order.setStatus(status);
        return orders.save(order);
    }

    private String code() {
        return "LH-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
