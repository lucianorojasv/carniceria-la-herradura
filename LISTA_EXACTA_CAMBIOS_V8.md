# Lista exacta de cambios del parche v8

## Base de datos

### `business_settings`

Se agregan:

- `assistant_name`
- `time_zone`
- `attention_days`
- `opening_time`
- `closing_time`
- `same_day_cutoff_time`
- `allow_next_day_reservations`
- `reservation_slots`
- `send_product_images`
- `yape_enabled`
- `yape_number`
- `yape_holder`
- `yape_qr_url`
- `plin_enabled`
- `plin_number`
- `plin_holder`
- `plin_qr_url`
- `transfer_enabled`
- `bank_name`
- `bank_account_type`
- `bank_account_number`
- `bank_cci`
- `bank_holder`

### `customer_orders`

Se agrega:

- `scheduled_for TIMESTAMPTZ`

### `chat_sessions`

Se amplía la restricción de estados para incluir:

- `SELECTING_RESERVATION_SLOT`

## Backend

### Nuevos archivos

- `BusinessHoursService.java`: calcula si está abierto, si todavía recibe pedidos del día, el próximo día de atención y las franjas de reserva.
- `V3__mashico_schedule_payments_media.sql`: migración para instalaciones con Flyway.

### Archivos modificados

- `BusinessSetting.java`: nuevos campos de horario, pagos, reservas y multimedia.
- `SettingsService.java`: valores predeterminados y normalización.
- `ChatState.java`: nuevo estado de selección de franja.
- `ChatMessageResponse.java`: agrega `mediaUrl` y `mediaType`.
- `OrderCreateRequest.java`: agrega `scheduledFor`.
- `CustomerOrder.java`: mapea `scheduled_for`.
- `OrderService.java`: guarda la programación y reserva stock.
- `ChatbotService.java`: horario, reserva, pagos, imágenes y reconocimiento por nombre.
- `WhatsAppService.java`: envío de mensajes de tipo `image` mediante Cloud API.
- `MediaController.java`: endpoint `/api/media/payment-qr`.
- `MediaStorageService.java`: carpeta `payment-qr` en Supabase Storage.

## Frontend

- `Settings.jsx`: panel completo de horarios, días, franjas, pagos, QR y configuración de Mashico.
- `Assistant.jsx`: muestra imágenes dentro del simulador.
- `Orders.jsx`: muestra reservas programadas.
- `styles.css`: estilos de configuración, QR, imágenes del chat y pedidos programados.
- `api.js`: conserva compatibilidad con `FormData` para subir QR sin establecer manualmente `Content-Type`.

## Flujos modificados

### Dentro del horario

```text
Pedido normal → carrito → modalidad → pago → confirmar
```

### Fuera del horario o después de la hora límite

```text
Pedido → se convierte en reserva → próximo día de atención → franja horaria → pago → confirmar
```

### Consulta directa de un corte

```text
Cliente: bistec
Mashico: imagen + descripción + precio + stock
```

### Promociones

```text
Cliente: promociones
Mashico: imagen del primer combo activo + lista de promociones
```
