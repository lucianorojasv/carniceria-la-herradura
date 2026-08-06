# Parche v9 — Tienda pública, pedidos web, ubicación y galería

Este parche se aplica sobre la versión v8 de **Carnicería La Herradura**.

## Incluye

- Rediseño completo y adaptable de la tienda pública.
- Catálogo con búsqueda, categorías y carrito persistente.
- Compra sin crear cuenta.
- Checkout en tres pasos: entrega, datos y pago.
- Pedidos para hoy o programados según horario real del negocio.
- Datos y QR configurables de Yape y Plin, además de transferencia.
- Consulta pública del estado del pedido mediante código y celular.
- Gestión de uno o varios locales.
- Google Maps, botón “Cómo llegar” y referencias del negocio.
- Galería administrable: fachada, interior, mostrador, vitrina, estacionamiento y referencias.
- Mashico responde consultas de ubicación y puede enviar una foto de la fachada.
- Página pública `/ubicacion`.
- Página pública `/pedido`.
- SEO básico y navegación móvil.

## Requisito previo

Debes tener integrada la versión v8. Antes de copiar archivos crea un respaldo:

```powershell
git branch respaldo-antes-v9
```

## 1. Ejecutar SQL en Supabase

Abre:

```text
Supabase → SQL Editor → New query
```

Ejecuta todo el archivo:

```text
supabase/07_store_locations_and_gallery.sql
```

El SQL crea:

- `store_locations`
- `store_location_images`
- índices y restricciones
- un local inicial basado en `business_settings`

Si la dirección actual está vacía o contiene “PENDIENTE”, el local inicial quedará oculto hasta que lo completes desde el panel.

## 2. Copiar el parche

Descomprime el ZIP dentro de la raíz del repositorio. Debes ver las carpetas:

```text
backend
frontend
supabase
```

Acepta reemplazar los archivos existentes.

## 3. Revisar cambios

```powershell
git status
git diff --stat
```

## 4. Subir a GitHub

```powershell
git add .
git commit -m "Agregar tienda publica ubicacion galeria y pedidos web"
git pull --rebase origin main
git push origin main
```

No utilices `git push --force`.

## 5. Despliegue

Render y Vercel deberían desplegar automáticamente al recibir el commit.

En Render verifica:

```text
Deploying → Live
```

En Vercel verifica:

```text
Building → Ready
```

No se agregan variables obligatorias nuevas. Se conservan las variables de Supabase y WhatsApp de la versión anterior.

## 6. Configurar el local

Entra al panel administrativo:

```text
Dashboard → Locales
```

Completa:

- nombre del local
- dirección
- distrito, provincia y departamento
- teléfono comercial y WhatsApp comercial
- latitud y longitud
- enlace de Google Maps
- URL del mapa insertado
- referencia
- estacionamiento
- local principal
- visible para clientes

### Obtener el mapa insertado

1. Abre el negocio en Google Maps.
2. Pulsa **Compartir**.
3. Selecciona **Insertar un mapa**.
4. Copia el iframe completo o solo su atributo `src`.
5. Pégalo en **URL del mapa insertado**. El formulario extrae automáticamente el `src` cuando pegas el iframe completo.

### Obtener la ruta

Puedes guardar un enlace compartido de Google Maps. Cuando existen latitud y longitud, el backend también construye automáticamente el botón **Cómo llegar**.

## 7. Subir fotografías

Desde el mismo módulo **Locales** selecciona un tipo:

```text
Portada
Fachada
Interior
Mostrador
Vitrina
Estacionamiento
Referencia
Galería
```

Se recomienda subir archivos JPG, PNG o WEBP menores de 4 MB. Cada local admite hasta 24 fotografías.

La fotografía marcada como `COVER` o `FACADE` se utiliza como imagen principal de la tienda y Mashico puede enviarla al responder por la ubicación.

## 8. Configurar pagos

Entra a:

```text
Dashboard → Configuración → Datos de pago
```

Activa únicamente los medios que utilizarás y completa:

- número y titular de Yape
- QR de Yape
- número y titular de Plin
- QR de Plin
- banco, tipo de cuenta, cuenta, CCI y titular

Los datos se muestran en el resumen del checkout y después de registrar el pedido. El sistema indica que el cliente debe pagar después de la confirmación del negocio.

## 9. URLs nuevas

```text
/catalogo             Tienda y catálogo
/ubicacion            Mapa, referencias y fotografías
/pedido               Consulta de pedidos
/pedido/{codigo}      Consulta con código precargado
/locales               Administración de locales
```

## 10. Prueba rápida

1. Agrega un producto al carrito.
2. Finaliza el pedido sin iniciar sesión.
3. Selecciona recojo o delivery.
4. Elige hoy o una reserva.
5. Confirma el pedido.
6. Copia el código generado.
7. Entra a `/pedido` y consulta con el código y celular.
8. Entra a `/ubicacion` y prueba **Cómo llegar**.
9. Escribe `ubicación` o `7` a Mashico.

## Alcance de facturación SUNAT

Este parche no activa la emisión real de boletas o facturas ante SUNAT. Esa integración debe implementarse como un módulo separado porque necesita RUC emisor, usuario SOL secundario, certificado digital y pruebas contra el entorno beta antes de producción. El checkout y los pedidos quedan preparados para integrar ese módulo en una siguiente versión sin mezclar credenciales tributarias en el frontend.
