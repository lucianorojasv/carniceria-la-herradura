package pe.laherradura.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.laherradura.dto.*;
import pe.laherradura.entity.*;
import pe.laherradura.enums.*;
import pe.laherradura.exception.BusinessException;
import pe.laherradura.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@Transactional
public class ChatbotService {
    private final ChatSessionRepository sessions;
    private final CustomerService customers;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final DeliveryZoneRepository zones;
    private final PromotionRepository promotions;
    private final OrderService orders;
    private final OpenAiRecommendationService ai;
    private final ObjectMapper mapper;

    public ChatbotService(ChatSessionRepository sessions, CustomerService customers, CategoryRepository categories,
                          ProductRepository products, DeliveryZoneRepository zones, PromotionRepository promotions,
                          OrderService orders, OpenAiRecommendationService ai, ObjectMapper mapper) {
        this.sessions = sessions; this.customers = customers; this.categories = categories; this.products = products;
        this.zones = zones; this.promotions = promotions; this.orders = orders; this.ai = ai; this.mapper = mapper;
    }

    public ChatMessageResponse process(String rawPhone, String customerName, String rawMessage) {
        String phone = customers.clean(rawPhone);
        String message = rawMessage == null ? "" : rawMessage.trim();
        Customer customer = customers.getOrCreate(phone, customerName);
        ChatSession session = sessions.findByPhone(phone).orElseGet(() -> {
            ChatSession s = new ChatSession(); s.setPhone(phone); s.setCustomer(customer); return sessions.save(s);
        });
        session.setCustomer(customer); session.setLastInteraction(OffsetDateTime.now());
        Context ctx = readContext(session);
        if (customerName != null && !customerName.isBlank()) { customer.setName(customerName.trim()); ctx.customerName = customerName.trim(); }
        String normalized = normalize(message);

        if (Set.of("reiniciar", "reset", "inicio", "menu", "menú").contains(normalized)) {
            reset(session, ctx); return response(session, menu(customer.getName()), null, false);
        }
        if (Set.of("asesor", "persona", "humano", "5").contains(normalized) && session.getState() == ChatState.MAIN_MENU) {
            session.setState(ChatState.HUMAN_HANDOFF); save(session, ctx);
            return response(session, "👤 Te comunicaré con una persona de *Carnicería La Herradura*. En breve continuará la atención.\n\nEscribe *menu* para volver al asistente.", null, true);
        }
        if (Set.of("cancelar", "cancel").contains(normalized)) {
            reset(session, ctx); return response(session, "Pedido cancelado. No se registró ningún cobro.\n\n" + menu(customer.getName()), null, false);
        }

        try {
            return switch (session.getState()) {
                case MAIN_MENU -> mainMenu(session, ctx, message, normalized);
                case SELECTING_CATEGORY -> selectCategory(session, ctx, message);
                case SELECTING_PRODUCT -> selectProduct(session, ctx, message);
                case ENTERING_QUANTITY -> enterQuantity(session, ctx, message);
                case CART_DECISION -> cartDecision(session, ctx, normalized);
                case SELECTING_FULFILLMENT -> selectFulfillment(session, ctx, normalized);
                case SELECTING_ZONE -> selectZone(session, ctx, message);
                case ENTERING_ADDRESS -> enterAddress(session, ctx, message);
                case ENTERING_REFERENCE -> enterReference(session, ctx, message);
                case SELECTING_PAYMENT -> selectPayment(session, ctx, normalized);
                case CONFIRMING -> confirm(session, ctx, normalized);
                case HUMAN_HANDOFF -> response(session, "Tu conversación está asignada a una persona. Escribe *menu* para volver al asistente automático.", null, true);
            };
        } catch (BusinessException e) {
            save(session, ctx); return response(session, "⚠️ " + e.getMessage() + "\n\nPuedes intentarlo nuevamente o escribir *menu*.", null, false);
        } catch (Exception e) {
            save(session, ctx); return response(session, "Ocurrió un inconveniente al procesar tu solicitud. Escribe *menu* para comenzar nuevamente.", null, false);
        }
    }

    private ChatMessageResponse mainMenu(ChatSession s, Context c, String message, String n) {
        if (n.equals("1") || n.contains("catalog") || n.contains("corte") || n.contains("precio")) {
            s.setState(ChatState.SELECTING_CATEGORY); save(s,c); return response(s, categoriesMessage(), null, false);
        }
        if (n.equals("2") || n.contains("pedido") || n.contains("comprar")) {
            s.setState(ChatState.SELECTING_CATEGORY); save(s,c); return response(s, "🛒 Vamos a armar tu pedido.\n\n" + categoriesMessage(), null, false);
        }
        if (n.equals("3") || n.contains("promoc") || n.contains("combo")) return response(s, promotionsMessage(), null, false);
        if (n.equals("4") || n.contains("delivery") || n.contains("envio") || n.contains("envío")) return response(s, zonesMessage(false), null, false);
        if (n.equals("5") || n.contains("asesor") || n.contains("persona")) {
            s.setState(ChatState.HUMAN_HANDOFF); save(s,c); return response(s,"👤 Te comunicaré con una persona. En breve continuará la atención.",null,true);
        }
        String recommendation = ai.recommend(message);
        return response(s, recommendation + "\n\n" + menu(s.getCustomer().getName()), null, false);
    }

    private ChatMessageResponse selectCategory(ChatSession s, Context c, String message) {
        List<Category> list = categories.findByActiveTrueOrderByNameAsc();
        int choice = choice(message, list.stream().map(Category::getName).toList());
        if (choice < 0) return response(s, "No identifiqué la categoría.\n\n" + categoriesMessage(), null, false);
        Category category = list.get(choice); c.categoryId = category.getId();
        List<Product> ps = products.findByCategoryIdAndActiveTrueOrderByNameAsc(category.getId());
        if (ps.isEmpty()) return response(s, "Por ahora no hay productos disponibles en esa categoría. Elige otra:\n\n" + categoriesMessage(), null, false);
        s.setState(ChatState.SELECTING_PRODUCT); save(s,c);
        return response(s, productsMessage(category, ps), null, false);
    }

    private ChatMessageResponse selectProduct(ChatSession s, Context c, String message) {
        if (c.categoryId == null) { s.setState(ChatState.SELECTING_CATEGORY); save(s,c); return response(s,categoriesMessage(),null,false); }
        List<Product> list = products.findByCategoryIdAndActiveTrueOrderByNameAsc(c.categoryId);
        int choice = choice(message, list.stream().map(Product::getName).toList());
        if (choice < 0) return response(s, "No identifiqué el producto. Responde con el número:\n\n" + productsMessage(null, list), null, false);
        Product p = list.get(choice); c.productId = p.getId(); s.setState(ChatState.ENTERING_QUANTITY); save(s,c);
        String unit = p.getUnit()==ProductUnit.KG?"kg":p.getUnit()==ProductUnit.UNIT?"unidad(es)":"paquete(s)";
        return response(s, "Seleccionaste *"+p.getName()+"* a S/ "+money(p.getPricePerUnit())+" por "+unit+".\nStock disponible: "+p.getStockQuantity()+".\n\n¿Cuánto deseas? Ejemplo: *1* o *0.5*", null, false);
    }

    private ChatMessageResponse enterQuantity(ChatSession s, Context c, String message) {
        if (c.productId == null) { s.setState(ChatState.SELECTING_CATEGORY); save(s,c); return response(s,categoriesMessage(),null,false); }
        Product p = products.findById(c.productId).orElseThrow(() -> new BusinessException("Producto no disponible"));
        BigDecimal qty;
        try { qty = new BigDecimal(message.replace(",", ".").replaceAll("[^0-9.]", "")); }
        catch (Exception e) { return response(s, "Ingresa una cantidad válida, por ejemplo *1* o *0.5*.", null, false); }
        if (qty.compareTo(p.getMinimumQuantity()) < 0) throw new BusinessException("La cantidad mínima es " + p.getMinimumQuantity());
        if (qty.compareTo(p.getStockQuantity()) > 0) throw new BusinessException("Solo tenemos " + p.getStockQuantity() + " disponibles");
        Optional<CartItem> existing = c.cart.stream().filter(i -> Objects.equals(i.productId, p.getId())).findFirst();
        if(existing.isPresent()) existing.get().quantity = existing.get().quantity.add(qty); else c.cart.add(new CartItem(p.getId(), p.getName(), qty, p.getPricePerUnit()));
        s.setState(ChatState.CART_DECISION); save(s,c);
        return response(s, "✅ Agregamos *"+qty+"* de *"+p.getName()+"*.\n\n"+cart(c)+"\n\n1️⃣ Agregar otro producto\n2️⃣ Finalizar pedido\n3️⃣ Ver carrito\n4️⃣ Cancelar", null, false);
    }

    private ChatMessageResponse cartDecision(ChatSession s, Context c, String n) {
        if(n.equals("1") || n.contains("otro") || n.contains("agregar")){s.setState(ChatState.SELECTING_CATEGORY);save(s,c);return response(s,categoriesMessage(),null,false);}
        if(n.equals("2") || n.contains("final")){s.setState(ChatState.SELECTING_FULFILLMENT);save(s,c);return response(s,"¿Cómo recibirás tu pedido?\n\n1️⃣ Recojo en tienda\n2️⃣ Delivery",null,false);}
        if(n.equals("3") || n.contains("carrito"))return response(s,cart(c)+"\n\n1️⃣ Agregar otro producto\n2️⃣ Finalizar pedido\n4️⃣ Cancelar",null,false);
        if(n.equals("4")){reset(s,c);return response(s,"Pedido cancelado.\n\n"+menu(s.getCustomer().getName()),null,false);}
        return response(s,"Elige una opción:\n1️⃣ Agregar otro producto\n2️⃣ Finalizar pedido\n3️⃣ Ver carrito\n4️⃣ Cancelar",null,false);
    }

    private ChatMessageResponse selectFulfillment(ChatSession s, Context c, String n) {
        if(n.equals("1") || n.contains("recojo") || n.contains("tienda")){c.fulfillmentType=FulfillmentType.PICKUP;s.setState(ChatState.SELECTING_PAYMENT);save(s,c);return response(s,paymentMessage(),null,false);}
        if(n.equals("2") || n.contains("delivery") || n.contains("envio") || n.contains("envío")){c.fulfillmentType=FulfillmentType.DELIVERY;s.setState(ChatState.SELECTING_ZONE);save(s,c);return response(s,zonesMessage(true),null,false);}
        return response(s,"Responde *1* para recojo en tienda o *2* para delivery.",null,false);
    }

    private ChatMessageResponse selectZone(ChatSession s, Context c, String message) {
        List<DeliveryZone> list=zones.findByActiveTrueOrderByNameAsc();int selected=choice(message,list.stream().map(DeliveryZone::getName).toList());
        if(selected<0)return response(s,"No identifiqué la zona.\n\n"+zonesMessage(true),null,false);
        DeliveryZone z=list.get(selected);c.zoneId=z.getId();s.setState(ChatState.ENTERING_ADDRESS);save(s,c);
        return response(s,"Zona seleccionada: *"+z.getName()+"*. Costo: S/ "+money(z.getFee())+".\n\nEscribe la dirección completa de entrega.",null,false);
    }

    private ChatMessageResponse enterAddress(ChatSession s, Context c, String message) {
        if(message.length()<8)return response(s,"Escribe una dirección más completa, incluyendo calle, número o referencia principal.",null,false);
        c.address=message.trim();s.setState(ChatState.ENTERING_REFERENCE);save(s,c);return response(s,"Ahora escribe una referencia para ubicarte. Si no tienes, responde *sin referencia*.",null,false);
    }

    private ChatMessageResponse enterReference(ChatSession s, Context c, String message) {
        c.reference=message.trim();s.setState(ChatState.SELECTING_PAYMENT);save(s,c);return response(s,paymentMessage(),null,false);
    }

    private ChatMessageResponse selectPayment(ChatSession s, Context c, String n) {
        PaymentMethod m = switch(n){case "1"->PaymentMethod.CASH;case "2"->PaymentMethod.YAPE;case "3"->PaymentMethod.PLIN;case "4"->PaymentMethod.TRANSFER;default->null;};
        if(m==null){if(n.contains("efect"))m=PaymentMethod.CASH;else if(n.contains("yape"))m=PaymentMethod.YAPE;else if(n.contains("plin"))m=PaymentMethod.PLIN;else if(n.contains("trans"))m=PaymentMethod.TRANSFER;}
        if(m==null)return response(s,paymentMessage(),null,false);
        c.paymentMethod=m;s.setState(ChatState.CONFIRMING);save(s,c);return response(s,summary(c)+"\n\nEscribe *CONFIRMAR* para registrar el pedido o *CANCELAR* para anularlo.",null,false);
    }

    private ChatMessageResponse confirm(ChatSession s, Context c, String n) {
        if(!n.equals("confirmar") && !n.equals("si") && !n.equals("sí"))return response(s,"Para registrar el pedido escribe *CONFIRMAR*. Para anularlo escribe *CANCELAR*.",null,false);
        if(c.cart.isEmpty())throw new BusinessException("El carrito está vacío");
        List<OrderCreateRequest.Item> items=c.cart.stream().map(i->new OrderCreateRequest.Item(i.productId,i.quantity)).toList();
        OrderCreateRequest req=new OrderCreateRequest(
            c.customerName==null?s.getCustomer().getName():c.customerName,s.getPhone(),
            c.fulfillmentType==null?FulfillmentType.PICKUP:c.fulfillmentType,c.zoneId,c.address,c.reference,
            c.paymentMethod==null?PaymentMethod.CASH:c.paymentMethod,OrderSource.WHATSAPP,"Pedido generado por Mashico",items);
        CustomerOrder order=orders.create(req);String code=order.getCode();reset(s,c);
        String delivery=order.getFulfillmentType()==FulfillmentType.DELIVERY?"Delivery a: "+order.getDeliveryAddress():"Recojo en tienda";
        return response(s,"✅ *PEDIDO CONFIRMADO*\n\nCódigo: *"+code+"*\nTotal: *S/ "+money(order.getTotal())+"*\nModalidad: "+delivery+"\n\nTe avisaremos cuando esté listo. ¡Gracias por comprar en Carnicería La Herradura!",code,false);
    }

    private String menu(String name){return "🥩 ¡Hola"+(name==null||name.isBlank()?"":" "+name)+"! Soy *Mashico*, asistente de *Carnicería La Herradura*.\n\n¿Qué deseas hacer?\n1️⃣ Ver cortes y precios\n2️⃣ Armar un pedido\n3️⃣ Combos y promociones\n4️⃣ Consultar delivery\n5️⃣ Hablar con una persona";}
    private String categoriesMessage(){List<Category> list=categories.findByActiveTrueOrderByNameAsc();if(list.isEmpty())return "El catálogo todavía está siendo configurado.";StringBuilder b=new StringBuilder("Elige una categoría:\n\n");for(int i=0;i<list.size();i++)b.append(i+1).append("️⃣ ").append(list.get(i).getName()).append("\n");return b.toString();}
    private String productsMessage(Category category,List<Product> list){StringBuilder b=new StringBuilder(category==null?"Productos disponibles:\n\n":"*"+category.getName()+"*\n\n");for(int i=0;i<list.size();i++){Product p=list.get(i);b.append(i+1).append(". ").append(p.getName()).append(" — S/ ").append(money(p.getPricePerUnit())).append(p.getUnit()==ProductUnit.KG?"/kg":"").append("\n");}b.append("\nResponde con el número del producto.");return b.toString();}
    private String promotionsMessage(){List<Promotion> list=promotions.findByActiveTrueOrderByStartDateDesc();if(list.isEmpty())return "🔥 Próximamente publicaremos combos y promociones. Escribe *2* para armar un pedido con los cortes disponibles.";StringBuilder b=new StringBuilder("🔥 *PROMOCIONES*\n\n");for(Promotion p:list)b.append("• *").append(p.getName()).append("*").append(p.getPromotionalPrice()==null?"":" — S/ "+money(p.getPromotionalPrice())).append("\n").append(p.getDescription()==null?"":p.getDescription()).append("\n\n");return b.toString();}
    private String zonesMessage(boolean choose){List<DeliveryZone> list=zones.findByActiveTrueOrderByNameAsc();if(list.isEmpty())return "El delivery está en etapa de configuración. Por ahora puedes elegir recojo en tienda o hablar con una persona.";StringBuilder b=new StringBuilder("🛵 *Zonas de delivery*\n\n");for(int i=0;i<list.size();i++){DeliveryZone z=list.get(i);b.append(i+1).append(". ").append(z.getName()).append(" — S/ ").append(money(z.getFee())).append(" (pedido mínimo S/ ").append(money(z.getMinimumOrder())).append(")\n");}if(choose)b.append("\nResponde con el número de tu zona.");return b.toString();}
    private String paymentMessage(){return "¿Cómo pagarás?\n\n1️⃣ Efectivo\n2️⃣ Yape\n3️⃣ Plin\n4️⃣ Transferencia";}
    private String cart(Context c){BigDecimal total=BigDecimal.ZERO;StringBuilder b=new StringBuilder("🛒 *Tu carrito*\n");for(CartItem i:c.cart){BigDecimal sub=i.price.multiply(i.quantity).setScale(2,RoundingMode.HALF_UP);total=total.add(sub);b.append("• ").append(i.name).append(" x ").append(i.quantity).append(" = S/ ").append(money(sub)).append("\n");}b.append("Subtotal: *S/ ").append(money(total)).append("*");return b.toString();}
    private String summary(Context c){BigDecimal subtotal=c.cart.stream().map(i->i.price.multiply(i.quantity)).reduce(BigDecimal.ZERO,BigDecimal::add);BigDecimal fee=BigDecimal.ZERO;String modality="Recojo en tienda";if(c.fulfillmentType==FulfillmentType.DELIVERY&&c.zoneId!=null){DeliveryZone z=zones.findById(c.zoneId).orElse(null);if(z!=null){fee=z.getFee();modality="Delivery — "+z.getName()+"\nDirección: "+c.address;}}return "📋 *RESUMEN DEL PEDIDO*\n\n"+cart(c)+"\nDelivery: S/ "+money(fee)+"\nTotal: *S/ "+money(subtotal.add(fee))+"*\nModalidad: "+modality+"\nPago: "+(c.paymentMethod==null?"Pendiente":c.paymentMethod.name());}
    private int choice(String message,List<String> names){String t=normalize(message);try{int n=Integer.parseInt(t.replaceAll("[^0-9]",""));if(n>=1&&n<=names.size())return n-1;}catch(Exception ignored){}for(int i=0;i<names.size();i++)if(normalize(names.get(i)).contains(t)||t.contains(normalize(names.get(i))))return i;return -1;}
    private String normalize(String s){return java.text.Normalizer.normalize(s==null?"":s,java.text.Normalizer.Form.NFD).replaceAll("\\p{M}","").trim().toLowerCase(Locale.ROOT);}
    private String money(BigDecimal v){return v==null?"0.00":v.setScale(2,RoundingMode.HALF_UP).toPlainString();}
    private ChatMessageResponse response(ChatSession s,String reply,String code,boolean handoff){sessions.save(s);return new ChatMessageResponse(reply,s.getState().name(),code,handoff);}
    private Context readContext(ChatSession s){try{return mapper.readValue(s.getContextJson(),Context.class);}catch(Exception e){return new Context();}}
    private void save(ChatSession s,Context c){try{s.setContextJson(mapper.writeValueAsString(c));}catch(Exception e){s.setContextJson("{}");}s.setLastInteraction(OffsetDateTime.now());sessions.save(s);}
    private void reset(ChatSession s,Context c){s.setState(ChatState.MAIN_MENU);c.clear();save(s,c);}

    public static class Context {
        public Long categoryId; public Long productId; public Long zoneId; public FulfillmentType fulfillmentType;
        public String address; public String reference; public PaymentMethod paymentMethod; public String customerName;
        public List<CartItem> cart=new ArrayList<>();
        public Context(){}
        public void clear(){categoryId=null;productId=null;zoneId=null;fulfillmentType=null;address=null;reference=null;paymentMethod=null;cart=new ArrayList<>();}
    }
    public static class CartItem {
        public Long productId; public String name; public BigDecimal quantity; public BigDecimal price;
        public CartItem(){} public CartItem(Long id,String n,BigDecimal q,BigDecimal p){productId=id;name=n;quantity=q;price=p;}
    }
}
