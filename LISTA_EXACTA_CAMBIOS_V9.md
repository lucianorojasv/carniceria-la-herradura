# Lista exacta de cambios — v9

## Backend

### Nuevos modelos

- `StoreLocation`
- `StoreLocationImage`

### Nuevos DTO

- `StoreLocationRequest`
- `StoreLocationImageRequest`
- `StoreLocationResponse`
- `PublicOrderResponse`

### Nuevos repositorios

- `StoreLocationRepository`
- `StoreLocationImageRepository`

### Nuevos servicios y controladores

- `StoreLocationService`
- `StoreLocationController`

### Endpoints administrativos

```text
GET    /api/store-locations
GET    /api/store-locations/{id}
POST   /api/store-locations
PUT    /api/store-locations/{id}
DELETE /api/store-locations/{id}
POST   /api/store-locations/{id}/images
PATCH  /api/store-locations/{locationId}/images/{imageId}
DELETE /api/store-locations/{locationId}/images/{imageId}
```

### Endpoints públicos

```text
GET  /api/public/store-locations
GET  /api/public/store-locations/{id}
POST /api/public/orders
GET  /api/public/orders/{code}?phone=...
```

### Catálogo público ampliado

`GET /api/public/catalog` ahora incluye:

```text
businessStatus
locations
mainLocation
```

### Reglas nuevas de pedidos

- fuerza `source=WEB` en pedidos públicos
- rechaza pedidos de hoy fuera del horario
- permite reservas solo cuando están habilitadas
- valida día de atención
- valida franja horaria configurada
- valida que el horario sea futuro
- valida delivery habilitado, zona y dirección
- valida que Yape, Plin o transferencia estén habilitados
- conserva el bloqueo de stock dentro de la transacción

### Mashico

- opción 7: ubicación y cómo llegar
- reconoce `ubicación`, `dirección`, `cómo llegar`, `dónde están` y variantes
- responde dirección, referencia, horario y enlace de ruta
- adjunta foto de portada, fachada o referencia cuando existe

### Almacenamiento

`MediaStorageService` agrega la ruta:

```text
business-gallery/{locationId}/{uuid}.ext
```

Formatos admitidos:

```text
JPG, PNG, WEBP
```

Límite:

```text
4 MB por imagen
24 imágenes por local
```

## Base de datos

### Migración Flyway

```text
V4__store_locations_and_gallery.sql
```

### SQL manual Supabase

```text
07_store_locations_and_gallery.sql
```

### Tablas

```text
store_locations
store_location_images
```

### Integridad

- rango válido de latitud y longitud
- un solo local principal activo
- tipos de fotografía restringidos
- orden de fotografía no negativo
- eliminación en cascada de metadatos de imágenes

## Frontend público

### Tienda rediseñada

- portada comercial adaptable
- estado de atención
- buscador de productos
- filtros por categoría
- tarjetas visuales
- promociones
- bloque de Mashico
- información de horario, delivery y pagos
- pie de contacto

### Carrito

- panel lateral
- cambio de cantidad
- eliminación de productos
- subtotal en tiempo real
- persistencia con `localStorage`

### Checkout

- compra sin registro
- recojo o delivery
- pedido para hoy
- pedido programado
- datos del cliente
- observaciones de corte/preparación
- método de pago
- QR y cuentas configuradas
- confirmación y código de seguimiento

### Seguimiento

Nueva página:

```text
/pedido
```

Muestra:

- estado actual
- línea de avance
- modalidad
- pago
- total
- fecha programada
- productos
- acceso a WhatsApp

### Ubicación

Nueva página:

```text
/ubicacion
```

Muestra:

- mapa
- botón Cómo llegar
- horario
- referencia
- estacionamiento
- teléfono
- galería y visor ampliado

## Frontend administrativo

Nueva opción:

```text
Dashboard → Locales
```

Permite:

- registrar varios locales
- definir el principal
- activar u ocultar
- guardar coordenadas y enlaces
- probar la ruta
- subir fotografías
- editar título, descripción, texto alternativo, tipo, orden y visibilidad

## Privacidad

Los nuevos componentes ya no utilizan un número personal codificado como respaldo. Los botones de WhatsApp solo aparecen cuando existe un teléfono comercial configurado.
