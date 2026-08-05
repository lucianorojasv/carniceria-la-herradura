# Arquitectura

```mermaid
flowchart LR
  C[Cliente] --> W[WhatsApp / Catálogo web]
  W --> N[Nginx]
  N --> R[React]
  N --> B[Spring Boot API]
  B --> P[(PostgreSQL)]
  B --> M[WhatsApp Cloud API]
  B -. opcional .-> O[OpenAI Responses API]
  B -. automatizaciones .-> A[n8n]
```

## Principios

- El catálogo y stock son la fuente de verdad.
- La IA no puede inventar precio, stock, pedido ni pago.
- Cada pedido guarda precio histórico en `order_items`.
- Al crear un pedido se descuenta stock; al cancelarlo se repone.
- El webhook puede funcionar sin OpenAI mediante flujo determinista.
- Las credenciales se cargan desde variables de entorno.

## Entidades

```mermaid
erDiagram
  CATEGORY ||--o{ PRODUCT : contiene
  CUSTOMER ||--o{ CUSTOMER_ORDER : realiza
  CUSTOMER_ORDER ||--|{ ORDER_ITEM : incluye
  PRODUCT ||--o{ ORDER_ITEM : vendido
  DELIVERY_ZONE ||--o{ CUSTOMER_ORDER : tarifa
  CUSTOMER ||--o{ CHAT_SESSION : conversa
```
