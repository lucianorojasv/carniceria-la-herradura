package pe.laherradura.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.laherradura.dto.ChatMessageResponse;
import pe.laherradura.dto.OrderCreateRequest;
import pe.laherradura.dto.StoreLocationResponse;
import pe.laherradura.entity.BusinessSetting;
import pe.laherradura.entity.Category;
import pe.laherradura.entity.ChatSession;
import pe.laherradura.entity.Customer;
import pe.laherradura.entity.CustomerOrder;
import pe.laherradura.entity.DeliveryZone;
import pe.laherradura.entity.Product;
import pe.laherradura.entity.Promotion;
import pe.laherradura.enums.ChatState;
import pe.laherradura.enums.FulfillmentType;
import pe.laherradura.enums.OrderSource;
import pe.laherradura.enums.PaymentMethod;
import pe.laherradura.enums.ProductUnit;
import pe.laherradura.exception.BusinessException;
import pe.laherradura.repository.CategoryRepository;
import pe.laherradura.repository.ChatSessionRepository;
import pe.laherradura.repository.DeliveryZoneRepository;
import pe.laherradura.repository.ProductRepository;
import pe.laherradura.repository.PromotionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class ChatbotService {
    private static final Locale ES_PE = new Locale("es", "PE");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", ES_PE);

    private final ChatSessionRepository sessions;
    private final CustomerService customers;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final DeliveryZoneRepository zones;
    private final PromotionRepository promotions;
    private final OrderService orders;
    private final OpenAiRecommendationService ai;
    private final SettingsService settingsService;
    private final BusinessHoursService hoursService;
    private final StoreLocationService storeLocationService;
    private final ObjectMapper mapper;

    public ChatbotService(ChatSessionRepository sessions,
                          CustomerService customers,
                          CategoryRepository categories,
                          ProductRepository products,
                          DeliveryZoneRepository zones,
                          PromotionRepository promotions,
                          OrderService orders,
                          OpenAiRecommendationService ai,
                          SettingsService settingsService,
                          BusinessHoursService hoursService,
                          StoreLocationService storeLocationService,
                          ObjectMapper mapper) {
        this.sessions = sessions;
        this.customers = customers;
        this.categories = categories;
        this.products = products;
        this.zones = zones;
        this.promotions = promotions;
        this.orders = orders;
        this.ai = ai;
        this.settingsService = settingsService;
        this.hoursService = hoursService;
        this.storeLocationService = storeLocationService;
        this.mapper = mapper;
    }

    public ChatMessageResponse process(String rawPhone, String customerName, String rawMessage) {
        String phone = customers.clean(rawPhone);
        String message = rawMessage == null ? "" : rawMessage.trim();
        Customer customer = customers.getOrCreate(phone, customerName);
        ChatSession session = sessions.findByPhone(phone).orElseGet(() -> {
            ChatSession newSession = new ChatSession();
            newSession.setPhone(phone);
            newSession.setCustomer(customer);
            return sessions.save(newSession);
        });

        session.setCustomer(customer);
        session.setLastInteraction(OffsetDateTime.now());
        Context context = readContext(session);
        if (customerName != null && !customerName.isBlank()) {
            customer.setName(customerName.trim());
            context.customerName = customerName.trim();
        }

        String normalized = normalize(message);
        if (Set.of("reiniciar", "reset", "inicio", "menu", "menú").contains(normalized)) {
            reset(session, context);
            return response(session, menu(customer.getName()), null, false);
        }
        if (Set.of("cancelar", "cancel").contains(normalized)) {
            reset(session, context);
            return response(session, "Pedido cancelado. No se registró ningún cobro.\n\n" + menu(customer.getName()), null, false);
        }
        if (session.getState() == ChatState.MAIN_MENU && locationIntent(normalized)) {
            return locationResponse(session);
        }

        try {
            return switch (session.getState()) {
                case MAIN_MENU -> mainMenu(session, context, message, normalized);
                case SELECTING_CATEGORY -> selectCategory(session, context, message);
                case SELECTING_PRODUCT -> selectProduct(session, context, message);
                case ENTERING_QUANTITY -> enterQuantity(session, context, message);
                case CART_DECISION -> cartDecision(session, context, normalized);
                case SELECTING_FULFILLMENT -> selectFulfillment(session, context, normalized);
                case SELECTING_ZONE -> selectZone(session, context, message);
                case ENTERING_ADDRESS -> enterAddress(session, context, message);
                case ENTERING_REFERENCE -> enterReference(session, context, message);
                case SELECTING_RESERVATION_SLOT -> selectReservationSlot(session, context, message);
                case SELECTING_PAYMENT -> selectPayment(session, context, normalized);
                case CONFIRMING -> confirm(session, context, normalized);
                case HUMAN_HANDOFF -> response(session,
                        "Tu conversación está asignada a una persona. Escribe *menu* para volver con Mashico.",
                        null, true);
            };
        } catch (BusinessException exception) {
            save(session, context);
            return response(session, "⚠️ " + exception.getMessage()
                    + "\n\nPuedes intentarlo nuevamente o escribir *menu*.", null, false);
        } catch (Exception exception) {
            save(session, context);
            return response(session,
                    "Ocurrió un inconveniente al procesar tu solicitud. Escribe *menu* para comenzar nuevamente.",
                    null, false);
        }
    }

    private ChatMessageResponse mainMenu(ChatSession session, Context context, String message, String normalized) {
        BusinessSetting settings = settingsService.get();

        if (normalized.contains("horario") || normalized.contains("abierto") || normalized.contains("atienden")) {
            return response(session, hoursMessage(settings), null, false);
        }

        if (normalized.equals("6") || normalized.contains("reserv")) {
            return startReservation(session, context, settings);
        }

        if (normalized.equals("1") || normalized.contains("catalog") || normalized.contains("corte") || normalized.contains("precio")) {
            session.setState(ChatState.SELECTING_CATEGORY);
            save(session, context);
            return response(session, categoriesMessage(), null, false);
        }

        if (normalized.equals("2") || normalized.contains("pedido") || normalized.contains("comprar")) {
            BusinessHoursService.Status status = hoursService.status(settings);
            if (!status.acceptsSameDay()) {
                if (!settings.isAllowNextDayReservations()) {
                    return response(session, outOfHoursMessage(settings), null, false);
                }
                context.reservation = true;
                context.scheduledDate = status.nextBusinessDate();
                session.setState(ChatState.SELECTING_CATEGORY);
                save(session, context);
                return response(session,
                        "🌙 Estamos fuera del horario para pedidos de hoy. Prepararemos tu pedido como *reserva para "
                                + hoursService.humanDate(context.scheduledDate) + "*.\n\n" + categoriesMessage(),
                        null, false);
            }
            context.reservation = false;
            context.scheduledDate = null;
            session.setState(ChatState.SELECTING_CATEGORY);
            save(session, context);
            return response(session, "🛒 Vamos a armar tu pedido para hoy.\n\n" + categoriesMessage(), null, false);
        }

        if (normalized.equals("3") || normalized.contains("promoc") || normalized.contains("combo")) {
            Promotion first = firstPromotionWithImage();
            return response(session, promotionsMessage(), null, false,
                    first == null ? null : first.getImageUrl(), "image");
        }

        if (normalized.equals("4") || normalized.contains("delivery") || normalized.contains("envio") || normalized.contains("envío")) {
            return response(session, zonesMessage(false), null, false);
        }

        if (normalized.equals("7") || locationIntent(normalized)) {
            return locationResponse(session);
        }

        if (normalized.equals("5") || normalized.contains("asesor") || normalized.contains("persona") || normalized.contains("humano")) {
            session.setState(ChatState.HUMAN_HANDOFF);
            save(session, context);
            return response(session, "👤 Te comunicaré con una persona. En breve continuará la atención.", null, true);
        }

        Product mentioned = findMentionedProduct(normalized);
        if (mentioned != null) {
            String unit = mentioned.getUnit() == ProductUnit.KG ? "/kg" : "";
            String reply = "🥩 *" + mentioned.getName() + "*\n"
                    + safe(mentioned.getDescription()) + "\n"
                    + "Precio: *S/ " + money(mentioned.getPricePerUnit()) + unit + "*\n"
                    + "Stock disponible: " + mentioned.getStockQuantity() + "\n\n"
                    + "Escribe *2* para armar un pedido.";
            return response(session, reply, null, false,
                    settings.isSendProductImages() ? mentioned.getImageUrl() : null, "image");
        }

        String recommendation = ai.recommend(message);
        return response(session, recommendation + "\n\n" + menu(session.getCustomer().getName()), null, false);
    }

    private ChatMessageResponse startReservation(ChatSession session, Context context, BusinessSetting settings) {
        if (!settings.isAllowNextDayReservations()) {
            return response(session, "Las reservas para el próximo día están desactivadas.\n\n" + hoursMessage(settings), null, false);
        }
        context.clear();
        context.reservation = true;
        context.scheduledDate = hoursService.nextBusinessDate(settings, hoursService.status(settings).now().toLocalDate());
        session.setState(ChatState.SELECTING_CATEGORY);
        save(session, context);
        return response(session,
                "📅 Vamos a reservar tu pedido para *" + hoursService.humanDate(context.scheduledDate) + "*.\n\n"
                        + categoriesMessage(), null, false);
    }

    private ChatMessageResponse selectCategory(ChatSession session, Context context, String message) {
        List<Category> list = categories.findByActiveTrueOrderByNameAsc();
        int selected = choice(message, list.stream().map(Category::getName).toList());
        if (selected < 0) return response(session, "No identifiqué la categoría.\n\n" + categoriesMessage(), null, false);

        Category category = list.get(selected);
        context.categoryId = category.getId();
        List<Product> available = products.findByCategoryIdAndActiveTrueOrderByNameAsc(category.getId());
        if (available.isEmpty()) {
            return response(session,
                    "Por ahora no hay productos disponibles en esa categoría. Elige otra:\n\n" + categoriesMessage(),
                    null, false);
        }
        session.setState(ChatState.SELECTING_PRODUCT);
        save(session, context);
        return response(session, productsMessage(category, available), null, false);
    }

    private ChatMessageResponse selectProduct(ChatSession session, Context context, String message) {
        if (context.categoryId == null) {
            session.setState(ChatState.SELECTING_CATEGORY);
            save(session, context);
            return response(session, categoriesMessage(), null, false);
        }

        List<Product> list = products.findByCategoryIdAndActiveTrueOrderByNameAsc(context.categoryId);
        int selected = choice(message, list.stream().map(Product::getName).toList());
        if (selected < 0) {
            return response(session,
                    "No identifiqué el producto. Responde con el número:\n\n" + productsMessage(null, list),
                    null, false);
        }

        Product product = list.get(selected);
        context.productId = product.getId();
        session.setState(ChatState.ENTERING_QUANTITY);
        save(session, context);

        String unit = product.getUnit() == ProductUnit.KG
                ? "kg" : product.getUnit() == ProductUnit.UNIT ? "unidad(es)" : "paquete(s)";
        String reply = "Seleccionaste *" + product.getName() + "* a S/ " + money(product.getPricePerUnit())
                + " por " + unit + ".\n"
                + safe(product.getDescription()) + "\n"
                + "Stock disponible: " + product.getStockQuantity() + ".\n\n"
                + "¿Cuánto deseas? Ejemplo: *1* o *0.5*";
        BusinessSetting settings = settingsService.get();
        return response(session, reply, null, false,
                settings.isSendProductImages() ? product.getImageUrl() : null, "image");
    }

    private ChatMessageResponse enterQuantity(ChatSession session, Context context, String message) {
        if (context.productId == null) {
            session.setState(ChatState.SELECTING_CATEGORY);
            save(session, context);
            return response(session, categoriesMessage(), null, false);
        }

        Product product = products.findById(context.productId)
                .orElseThrow(() -> new BusinessException("Producto no disponible"));
        BigDecimal quantity;
        try {
            quantity = new BigDecimal(message.replace(",", ".").replaceAll("[^0-9.]", ""));
        } catch (Exception exception) {
            return response(session, "Ingresa una cantidad válida, por ejemplo *1* o *0.5*.", null, false);
        }

        if (quantity.compareTo(product.getMinimumQuantity()) < 0) {
            throw new BusinessException("La cantidad mínima es " + product.getMinimumQuantity());
        }
        if (quantity.compareTo(product.getStockQuantity()) > 0) {
            throw new BusinessException("Solo tenemos " + product.getStockQuantity() + " disponibles");
        }

        Optional<CartItem> existing = context.cart.stream()
                .filter(item -> Objects.equals(item.productId, product.getId()))
                .findFirst();
        if (existing.isPresent()) {
            existing.get().quantity = existing.get().quantity.add(quantity);
        } else {
            context.cart.add(new CartItem(product.getId(), product.getName(), quantity, product.getPricePerUnit()));
        }

        session.setState(ChatState.CART_DECISION);
        save(session, context);
        return response(session,
                "✅ Agregamos *" + quantity + "* de *" + product.getName() + "*.\n\n"
                        + cart(context)
                        + "\n\n1️⃣ Agregar otro producto\n2️⃣ Finalizar pedido\n3️⃣ Ver carrito\n4️⃣ Cancelar",
                null, false);
    }

    private ChatMessageResponse cartDecision(ChatSession session, Context context, String normalized) {
        if (normalized.equals("1") || normalized.contains("otro") || normalized.contains("agregar")) {
            session.setState(ChatState.SELECTING_CATEGORY);
            save(session, context);
            return response(session, categoriesMessage(), null, false);
        }
        if (normalized.equals("2") || normalized.contains("final")) {
            BusinessSetting settings = settingsService.get();
            BusinessHoursService.Status status = hoursService.status(settings);
            if (!context.reservation && !status.acceptsSameDay()) {
                if (!settings.isAllowNextDayReservations()) {
                    return response(session, outOfHoursMessage(settings), null, false);
                }
                context.reservation = true;
                context.scheduledDate = status.nextBusinessDate();
            }
            session.setState(ChatState.SELECTING_FULFILLMENT);
            save(session, context);
            String prefix = context.reservation
                    ? "📅 Este pedido quedará reservado para *" + hoursService.humanDate(context.scheduledDate) + "*.\n\n"
                    : "";
            return response(session, prefix + "¿Cómo recibirás tu pedido?\n\n1️⃣ Recojo en tienda\n2️⃣ Delivery", null, false);
        }
        if (normalized.equals("3") || normalized.contains("carrito")) {
            return response(session, cart(context)
                    + "\n\n1️⃣ Agregar otro producto\n2️⃣ Finalizar pedido\n4️⃣ Cancelar", null, false);
        }
        if (normalized.equals("4")) {
            reset(session, context);
            return response(session, "Pedido cancelado.\n\n" + menu(session.getCustomer().getName()), null, false);
        }
        return response(session,
                "Elige una opción:\n1️⃣ Agregar otro producto\n2️⃣ Finalizar pedido\n3️⃣ Ver carrito\n4️⃣ Cancelar",
                null, false);
    }

    private ChatMessageResponse selectFulfillment(ChatSession session, Context context, String normalized) {
        if (normalized.equals("1") || normalized.contains("recojo") || normalized.contains("tienda")) {
            context.fulfillmentType = FulfillmentType.PICKUP;
            if (context.reservation) {
                session.setState(ChatState.SELECTING_RESERVATION_SLOT);
                save(session, context);
                return response(session, reservationSlotsMessage(context), null, false);
            }
            session.setState(ChatState.SELECTING_PAYMENT);
            save(session, context);
            return response(session, paymentMessage(), null, false);
        }
        if (normalized.equals("2") || normalized.contains("delivery") || normalized.contains("envio") || normalized.contains("envío")) {
            context.fulfillmentType = FulfillmentType.DELIVERY;
            session.setState(ChatState.SELECTING_ZONE);
            save(session, context);
            return response(session, zonesMessage(true), null, false);
        }
        return response(session, "Responde *1* para recojo en tienda o *2* para delivery.", null, false);
    }

    private ChatMessageResponse selectZone(ChatSession session, Context context, String message) {
        List<DeliveryZone> list = zones.findByActiveTrueOrderByNameAsc();
        int selected = choice(message, list.stream().map(DeliveryZone::getName).toList());
        if (selected < 0) return response(session, "No identifiqué la zona.\n\n" + zonesMessage(true), null, false);

        DeliveryZone zone = list.get(selected);
        context.zoneId = zone.getId();
        session.setState(ChatState.ENTERING_ADDRESS);
        save(session, context);
        return response(session,
                "Zona seleccionada: *" + zone.getName() + "*. Costo: S/ " + money(zone.getFee())
                        + ".\n\nEscribe la dirección completa de entrega.",
                null, false);
    }

    private ChatMessageResponse enterAddress(ChatSession session, Context context, String message) {
        if (message.length() < 8) {
            return response(session,
                    "Escribe una dirección más completa, incluyendo calle, número o referencia principal.",
                    null, false);
        }
        context.address = message.trim();
        session.setState(ChatState.ENTERING_REFERENCE);
        save(session, context);
        return response(session,
                "Ahora escribe una referencia para ubicarte. Si no tienes, responde *sin referencia*.",
                null, false);
    }

    private ChatMessageResponse enterReference(ChatSession session, Context context, String message) {
        context.reference = message.trim();
        if (context.reservation) {
            session.setState(ChatState.SELECTING_RESERVATION_SLOT);
            save(session, context);
            return response(session, reservationSlotsMessage(context), null, false);
        }
        session.setState(ChatState.SELECTING_PAYMENT);
        save(session, context);
        return response(session, paymentMessage(), null, false);
    }

    private ChatMessageResponse selectReservationSlot(ChatSession session, Context context, String message) {
        BusinessSetting settings = settingsService.get();
        List<String> slots = hoursService.reservationSlots(settings);
        if (slots.isEmpty()) throw new BusinessException("No hay horarios de reserva configurados");

        int selected = choice(message, slots);
        if (selected < 0) return response(session, reservationSlotsMessage(context), null, false);
        if (context.scheduledDate == null) {
            context.scheduledDate = hoursService.nextBusinessDate(settings, hoursService.status(settings).now().toLocalDate());
        }
        context.scheduledSlot = slots.get(selected);
        context.scheduledFor = hoursService.scheduledStart(settings, context.scheduledDate, context.scheduledSlot);
        session.setState(ChatState.SELECTING_PAYMENT);
        save(session, context);
        return response(session,
                "⏰ Reserva programada para *" + hoursService.humanDate(context.scheduledDate)
                        + " de " + context.scheduledSlot + "*.\n\n" + paymentMessage(),
                null, false);
    }

    private ChatMessageResponse selectPayment(ChatSession session, Context context, String normalized) {
        BusinessSetting settings = settingsService.get();
        PaymentMethod method = paymentMethod(normalized);
        if (method == null) return response(session, paymentMessage(), null, false);
        if (method == PaymentMethod.YAPE && !settings.isYapeEnabled()) {
            return response(session, "Yape todavía no está habilitado.\n\n" + paymentMessage(), null, false);
        }
        if (method == PaymentMethod.PLIN && !settings.isPlinEnabled()) {
            return response(session, "Plin todavía no está habilitado.\n\n" + paymentMessage(), null, false);
        }
        if (method == PaymentMethod.TRANSFER && !settings.isTransferEnabled()) {
            return response(session, "La transferencia todavía no está habilitada.\n\n" + paymentMessage(), null, false);
        }

        context.paymentMethod = method;
        session.setState(ChatState.CONFIRMING);
        save(session, context);

        String instructions = paymentInstructions(settings, method, total(context));
        String qrUrl = switch (method) {
            case YAPE -> settings.getYapeQrUrl();
            case PLIN -> settings.getPlinQrUrl();
            default -> null;
        };
        return response(session,
                summary(context) + instructions
                        + "\n\nEscribe *CONFIRMAR* para registrar el pedido o *CANCELAR* para anularlo.",
                null, false, qrUrl, "image");
    }

    private ChatMessageResponse confirm(ChatSession session, Context context, String normalized) {
        if (!normalized.equals("confirmar") && !normalized.equals("si") && !normalized.equals("sí")) {
            return response(session,
                    "Para registrar el pedido escribe *CONFIRMAR*. Para anularlo escribe *CANCELAR*.",
                    null, false);
        }
        if (context.cart.isEmpty()) throw new BusinessException("El carrito está vacío");

        List<OrderCreateRequest.Item> items = context.cart.stream()
                .map(item -> new OrderCreateRequest.Item(item.productId, item.quantity))
                .toList();
        String notes = context.reservation ? "Pedido programado por Mashico" : "Pedido generado por Mashico";
        OrderCreateRequest request = new OrderCreateRequest(
                context.customerName == null ? session.getCustomer().getName() : context.customerName,
                session.getPhone(),
                context.fulfillmentType == null ? FulfillmentType.PICKUP : context.fulfillmentType,
                context.zoneId,
                context.address,
                context.reference,
                context.paymentMethod == null ? PaymentMethod.CASH : context.paymentMethod,
                OrderSource.WHATSAPP,
                notes,
                context.scheduledFor,
                items);

        CustomerOrder order = orders.create(request);
        String code = order.getCode();
        String delivery = order.getFulfillmentType() == FulfillmentType.DELIVERY
                ? "Delivery a: " + order.getDeliveryAddress()
                : "Recojo en tienda";
        String schedule = order.getScheduledFor() == null
                ? ""
                : "\nProgramado: *" + DATE_TIME_FORMAT.format(order.getScheduledFor()) + "*";
        reset(session, context);

        return response(session,
                "✅ *PEDIDO CONFIRMADO*\n\nCódigo: *" + code + "*\nTotal: *S/ " + money(order.getTotal())
                        + "*\nModalidad: " + delivery + schedule
                        + "\n\nTe avisaremos cuando esté listo. ¡Gracias por comprar en Carnicería La Herradura!",
                code, false);
    }

    private String menu(String name) {
        BusinessSetting settings = settingsService.get();
        BusinessHoursService.Status status = hoursService.status(settings);
        String statusText = status.acceptsSameDay()
                ? "🟢 Estamos atendiendo pedidos para hoy."
                : "🌙 Estamos fuera del horario para pedidos de hoy. Puedes reservar para "
                        + hoursService.humanDate(status.nextBusinessDate()) + ".";
        return "🥩 ¡Hola" + (name == null || name.isBlank() ? "" : " " + name) + "! Soy *"
                + settings.getAssistantName() + "*, asistente de *" + settings.getBusinessName() + "*.\n\n"
                + statusText + "\n\n¿Qué deseas hacer?\n"
                + "1️⃣ Ver cortes y precios\n"
                + "2️⃣ Armar un pedido\n"
                + "3️⃣ Combos y promociones\n"
                + "4️⃣ Consultar delivery\n"
                + "5️⃣ Hablar con una persona\n"
                + "6️⃣ Reservar para el próximo día de atención\n"
                + "7️⃣ Ver ubicación y cómo llegar\n\n"
                + "También puedes escribir *horario*, *ubicación* o el nombre de un corte, por ejemplo *bistec*.";
    }

    private String categoriesMessage() {
        List<Category> list = categories.findByActiveTrueOrderByNameAsc();
        if (list.isEmpty()) return "El catálogo todavía está siendo configurado.";
        StringBuilder builder = new StringBuilder("Elige una categoría:\n\n");
        for (int i = 0; i < list.size(); i++) {
            builder.append(i + 1).append(". ").append(list.get(i).getName()).append("\n");
        }
        return builder.toString();
    }

    private String productsMessage(Category category, List<Product> list) {
        StringBuilder builder = new StringBuilder(category == null
                ? "Productos disponibles:\n\n"
                : "*" + category.getName() + "*\n\n");
        for (int i = 0; i < list.size(); i++) {
            Product product = list.get(i);
            builder.append(i + 1).append(". ").append(product.getName())
                    .append(" — S/ ").append(money(product.getPricePerUnit()))
                    .append(product.getUnit() == ProductUnit.KG ? "/kg" : "")
                    .append("\n");
        }
        builder.append("\nResponde con el número del producto.");
        return builder.toString();
    }

    private String promotionsMessage() {
        List<Promotion> list = promotions.findByActiveTrueOrderByStartDateDesc();
        if (list.isEmpty()) {
            return "🔥 Próximamente publicaremos combos y promociones. Escribe *2* para armar un pedido.";
        }
        StringBuilder builder = new StringBuilder("🔥 *PROMOCIONES*\n\n");
        for (Promotion promotion : list) {
            builder.append("• *").append(promotion.getName()).append("*")
                    .append(promotion.getPromotionalPrice() == null ? "" : " — S/ " + money(promotion.getPromotionalPrice()))
                    .append("\n").append(safe(promotion.getDescription())).append("\n\n");
        }
        builder.append("Escribe *2* para armar tu pedido.");
        return builder.toString();
    }

    private Promotion firstPromotionWithImage() {
        return promotions.findByActiveTrueOrderByStartDateDesc().stream()
                .filter(promotion -> promotion.getImageUrl() != null && !promotion.getImageUrl().isBlank())
                .findFirst().orElse(null);
    }

    private String zonesMessage(boolean choose) {
        List<DeliveryZone> list = zones.findByActiveTrueOrderByNameAsc();
        if (list.isEmpty()) {
            return "El delivery está en etapa de configuración. Por ahora puedes elegir recojo en tienda o hablar con una persona.";
        }
        StringBuilder builder = new StringBuilder("🛵 *Zonas de delivery*\n\n");
        for (int i = 0; i < list.size(); i++) {
            DeliveryZone zone = list.get(i);
            builder.append(i + 1).append(". ").append(zone.getName())
                    .append(" — S/ ").append(money(zone.getFee()))
                    .append(" (pedido mínimo S/ ").append(money(zone.getMinimumOrder())).append(")\n");
        }
        if (choose) builder.append("\nResponde con el número de tu zona.");
        return builder.toString();
    }

    private String reservationSlotsMessage(Context context) {
        BusinessSetting settings = settingsService.get();
        List<String> slots = hoursService.reservationSlots(settings);
        StringBuilder builder = new StringBuilder("⏰ Elige el horario para *")
                .append(hoursService.humanDate(context.scheduledDate)).append("*:\n\n");
        for (int i = 0; i < slots.size(); i++) {
            builder.append(i + 1).append(". ").append(slots.get(i)).append("\n");
        }
        builder.append("\nResponde con el número del horario.");
        return builder.toString();
    }

    private String paymentMessage() {
        BusinessSetting settings = settingsService.get();
        StringBuilder builder = new StringBuilder("¿Cómo pagarás?\n\n1️⃣ Efectivo\n");
        if (settings.isYapeEnabled()) builder.append("2️⃣ Yape\n");
        if (settings.isPlinEnabled()) builder.append("3️⃣ Plin\n");
        if (settings.isTransferEnabled()) builder.append("4️⃣ Transferencia\n");
        if (!settings.isYapeEnabled() && !settings.isPlinEnabled() && !settings.isTransferEnabled()) {
            builder.append("\nPor ahora solo está habilitado el pago en efectivo.");
        }
        return builder.toString();
    }

    private PaymentMethod paymentMethod(String normalized) {
        PaymentMethod method = switch (normalized) {
            case "1" -> PaymentMethod.CASH;
            case "2" -> PaymentMethod.YAPE;
            case "3" -> PaymentMethod.PLIN;
            case "4" -> PaymentMethod.TRANSFER;
            default -> null;
        };
        if (method == null) {
            if (normalized.contains("efect")) method = PaymentMethod.CASH;
            else if (normalized.contains("yape")) method = PaymentMethod.YAPE;
            else if (normalized.contains("plin")) method = PaymentMethod.PLIN;
            else if (normalized.contains("trans")) method = PaymentMethod.TRANSFER;
        }
        return method;
    }

    private String paymentInstructions(BusinessSetting settings, PaymentMethod method, BigDecimal total) {
        return switch (method) {
            case CASH -> "\n\n💵 Pagarás en efectivo. Ten listo el monto de *S/ " + money(total) + "*.";
            case YAPE -> "\n\n💜 *PAGO CON YAPE*\nNúmero: *" + safe(settings.getYapeNumber())
                    + "*\nTitular: " + safe(settings.getYapeHolder())
                    + "\nMonto: *S/ " + money(total) + "*\nConserva el comprobante para validación.";
            case PLIN -> "\n\n🟢 *PAGO CON PLIN*\nNúmero: *" + safe(settings.getPlinNumber())
                    + "*\nTitular: " + safe(settings.getPlinHolder())
                    + "\nMonto: *S/ " + money(total) + "*\nConserva el comprobante para validación.";
            case TRANSFER -> "\n\n🏦 *TRANSFERENCIA BANCARIA*\nBanco: " + safe(settings.getBankName())
                    + "\nTipo de cuenta: " + safe(settings.getBankAccountType())
                    + "\nCuenta: *" + safe(settings.getBankAccountNumber()) + "*"
                    + "\nCCI: *" + safe(settings.getBankCci()) + "*"
                    + "\nTitular: " + safe(settings.getBankHolder())
                    + "\nMonto: *S/ " + money(total) + "*\nConserva el comprobante para validación.";
        };
    }

    private String cart(Context context) {
        BigDecimal total = BigDecimal.ZERO;
        StringBuilder builder = new StringBuilder("🛒 *Tu carrito*\n");
        for (CartItem item : context.cart) {
            BigDecimal subtotal = item.price.multiply(item.quantity).setScale(2, RoundingMode.HALF_UP);
            total = total.add(subtotal);
            builder.append("• ").append(item.name).append(" x ").append(item.quantity)
                    .append(" = S/ ").append(money(subtotal)).append("\n");
        }
        builder.append("Subtotal: *S/ ").append(money(total)).append("*");
        return builder.toString();
    }

    private String summary(Context context) {
        BigDecimal subtotal = subtotal(context);
        BigDecimal fee = deliveryFee(context);
        String modality = "Recojo en tienda";
        if (context.fulfillmentType == FulfillmentType.DELIVERY && context.zoneId != null) {
            DeliveryZone zone = zones.findById(context.zoneId).orElse(null);
            if (zone != null) modality = "Delivery — " + zone.getName() + "\nDirección: " + context.address;
        }
        String schedule = context.scheduledFor == null
                ? ""
                : "\nProgramado: *" + DATE_TIME_FORMAT.format(context.scheduledFor) + "*";
        return "📋 *RESUMEN DEL PEDIDO*\n\n" + cart(context)
                + "\nDelivery: S/ " + money(fee)
                + "\nTotal: *S/ " + money(subtotal.add(fee)) + "*"
                + "\nModalidad: " + modality + schedule
                + "\nPago: " + paymentLabel(context.paymentMethod);
    }

    private String paymentLabel(PaymentMethod method) {
        if (method == null) return "Pendiente";
        return switch (method) {
            case CASH -> "Efectivo";
            case YAPE -> "Yape";
            case PLIN -> "Plin";
            case TRANSFER -> "Transferencia";
        };
    }

    private String hoursMessage(BusinessSetting settings) {
        BusinessHoursService.Status status = hoursService.status(settings);
        String current = status.open() ? "🟢 En este momento estamos atendiendo." : "🌙 En este momento estamos fuera de horario.";
        return "🕒 *HORARIO DE ATENCIÓN*\n\n" + hoursService.scheduleDescription(settings)
                + "\n" + current
                + (settings.isAllowNextDayReservations()
                ? "\nPuedes escribir *reservar* para programar un pedido para " + hoursService.humanDate(status.nextBusinessDate()) + "."
                : "");
    }

    private String outOfHoursMessage(BusinessSetting settings) {
        BusinessHoursService.Status status = hoursService.status(settings);
        return "🌙 En este momento no recibimos pedidos para hoy.\n\n"
                + hoursService.scheduleDescription(settings)
                + (settings.isAllowNextDayReservations()
                ? "\n\nEscribe *reservar* para programar un pedido para " + hoursService.humanDate(status.nextBusinessDate()) + "."
                : "");
    }

    private ChatMessageResponse locationResponse(ChatSession session) {
        StoreLocationResponse location = storeLocationService.mainPublic();
        if (location == null) {
            BusinessSetting settings = settingsService.get();
            return response(session,
                    "📍 Nuestra ubicación todavía está siendo configurada.\nDirección: " + safe(settings.getAddress())
                            + "\n\nEscribe *5* para hablar con una persona.",
                    null, false);
        }
        StringBuilder message = new StringBuilder("📍 *").append(location.name()).append("*\n")
                .append(location.address());
        if (location.district() != null && !location.district().isBlank()) message.append(", ").append(location.district());
        if (location.referenceText() != null && !location.referenceText().isBlank()) {
            message.append("\nReferencia: ").append(location.referenceText());
        }
        message.append("\nHorario: ").append(location.todaySchedule())
                .append("\n\n🗺️ Cómo llegar:\n").append(location.directionsUrl());
        String media = location.images().stream()
                .filter(image -> Set.of("COVER", "FACADE", "REFERENCE").contains(image.imageType()))
                .map(StoreLocationResponse.Image::imageUrl)
                .findFirst()
                .orElseGet(() -> location.images().stream().map(StoreLocationResponse.Image::imageUrl).findFirst().orElse(null));
        return response(session, message.toString(), null, false, media, "image");
    }

    private boolean locationIntent(String normalized) {
        if (normalized == null || normalized.isBlank()) return false;
        return normalized.equals("7")
                || normalized.equals("ubicacion")
                || normalized.equals("direccion")
                || normalized.equals("como llegar")
                || normalized.equals("donde estan")
                || normalized.equals("donde queda")
                || normalized.contains("ubicacion del local")
                || normalized.contains("direccion del local")
                || normalized.contains("como llego");
    }

    private Product findMentionedProduct(String normalizedMessage) {
        if (normalizedMessage == null || normalizedMessage.length() < 3) return null;
        for (Product product : products.findByActiveTrueOrderByNameAsc()) {
            String productName = normalize(product.getName());
            if (normalizedMessage.equals(productName) || normalizedMessage.contains(productName)) return product;
        }
        return null;
    }

    private BigDecimal subtotal(Context context) {
        return context.cart.stream()
                .map(item -> item.price.multiply(item.quantity))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal deliveryFee(Context context) {
        if (context.fulfillmentType != FulfillmentType.DELIVERY || context.zoneId == null) return BigDecimal.ZERO;
        return zones.findById(context.zoneId).map(DeliveryZone::getFee).orElse(BigDecimal.ZERO);
    }

    private BigDecimal total(Context context) {
        return subtotal(context).add(deliveryFee(context));
    }

    private int choice(String message, List<String> names) {
        String text = normalize(message);
        try {
            int selected = Integer.parseInt(text.replaceAll("[^0-9]", ""));
            if (selected >= 1 && selected <= names.size()) return selected - 1;
        } catch (Exception ignored) {
            // Intenta luego por nombre.
        }
        for (int i = 0; i < names.size(); i++) {
            String candidate = normalize(names.get(i));
            if (candidate.contains(text) || text.contains(candidate)) return i;
        }
        return -1;
    }

    private String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String money(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Por configurar" : value.trim();
    }

    private ChatMessageResponse response(ChatSession session, String reply, String code, boolean handoff) {
        return response(session, reply, code, handoff, null, null);
    }

    private ChatMessageResponse response(ChatSession session, String reply, String code, boolean handoff,
                                         String mediaUrl, String mediaType) {
        sessions.save(session);
        String validUrl = mediaUrl == null || mediaUrl.isBlank() ? null : mediaUrl.trim();
        return new ChatMessageResponse(reply, session.getState().name(), code, handoff, validUrl,
                validUrl == null ? null : mediaType);
    }

    private Context readContext(ChatSession session) {
        try {
            Context context = mapper.readValue(session.getContextJson(), Context.class);
            if (context.cart == null) context.cart = new ArrayList<>();
            return context;
        } catch (Exception exception) {
            return new Context();
        }
    }

    private void save(ChatSession session, Context context) {
        try {
            session.setContextJson(mapper.writeValueAsString(context));
        } catch (Exception exception) {
            session.setContextJson("{}");
        }
        session.setLastInteraction(OffsetDateTime.now());
        sessions.save(session);
    }

    private void reset(ChatSession session, Context context) {
        session.setState(ChatState.MAIN_MENU);
        context.clear();
        save(session, context);
    }

    public static class Context {
        public Long categoryId;
        public Long productId;
        public Long zoneId;
        public FulfillmentType fulfillmentType;
        public String address;
        public String reference;
        public PaymentMethod paymentMethod;
        public String customerName;
        public boolean reservation;
        public LocalDate scheduledDate;
        public String scheduledSlot;
        public OffsetDateTime scheduledFor;
        public List<CartItem> cart = new ArrayList<>();

        public Context() { }

        public void clear() {
            categoryId = null;
            productId = null;
            zoneId = null;
            fulfillmentType = null;
            address = null;
            reference = null;
            paymentMethod = null;
            reservation = false;
            scheduledDate = null;
            scheduledSlot = null;
            scheduledFor = null;
            cart = new ArrayList<>();
        }
    }

    public static class CartItem {
        public Long productId;
        public String name;
        public BigDecimal quantity;
        public BigDecimal price;

        public CartItem() { }

        public CartItem(Long productId, String name, BigDecimal quantity, BigDecimal price) {
            this.productId = productId;
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }
    }
}
