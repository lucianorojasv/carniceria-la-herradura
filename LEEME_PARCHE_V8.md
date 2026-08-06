# Parche v8 — Mashico con horarios, reservas, pagos e imágenes en WhatsApp

Este parche se aplica **después del parche v7**. No reemplaza la corrección CORS ni cambia las variables de WhatsApp que ya configuraste.

## Funciones agregadas

1. Horario real configurable por días y horas usando la zona `America/Lima`.
2. Hora límite para aceptar pedidos del mismo día.
3. Reserva automática para el próximo día de atención cuando el negocio está cerrado.
4. Franjas horarias configurables para recojo o delivery programado.
5. Campo `scheduled_for` para guardar la fecha y hora reservada en cada pedido.
6. Datos configurables de Yape, Plin y transferencia bancaria.
7. Carga de imágenes QR desde el dashboard hacia Supabase Storage.
8. Mashico envía la fotografía del corte seleccionado por WhatsApp.
9. Mashico reconoce mensajes como `bistec`, muestra precio, stock, descripción e imagen.
10. Mashico envía la imagen del primer combo activo al consultar promociones.
11. El simulador del dashboard muestra las imágenes enviadas por Mashico.
12. La bandeja Pedidos distingue pedidos para hoy y reservas programadas.

## IMPORTANTE: orden de instalación

Como Render usa actualmente:

```env
FLYWAY_ENABLED=false
JPA_DDL_AUTO=none
```

primero debes ejecutar el SQL y después desplegar el código. Si despliegas Java antes de crear las columnas, el backend puede fallar al consultar `business_settings` o `customer_orders`.

## Paso 1. Ejecutar SQL en Supabase

Abre:

```text
Supabase → SQL Editor → New query
```

Copia y ejecuta el archivo:

```text
supabase/06_mashico_schedule_payments_media.sql
```

El script es repetible: utiliza `IF NOT EXISTS` y puede ejecutarse nuevamente sin duplicar columnas.

## Paso 2. Copiar el parche

Copia las carpetas del ZIP sobre la raíz del proyecto:

```text
backend
frontend
supabase
```

Acepta reemplazar los archivos existentes.

## Paso 3. Subir a GitHub

```powershell
git status
git add .
git commit -m "Agregar horarios reservas pagos e imágenes a Mashico"
git pull --rebase origin main
git push origin main
```

Si el `pull --rebase` muestra un conflicto, no uses `git rebase --skip`. Resuelve el archivo, ejecuta `git add .` y continúa con `git rebase --continue`.

## Paso 4. Desplegar

Vercel y Render deberían desplegar automáticamente. En Render también puedes usar:

```text
Manual Deploy → Deploy latest commit
```

Espera hasta ver `Live`.

## Paso 5. Configurar desde el dashboard

Entra a:

```text
Dashboard → Configuración
```

Completa estas secciones:

### Horario y reservas

- Zona horaria: `America/Lima`
- Días de atención
- Hora de apertura
- Hora de cierre
- Última hora para pedidos del día
- Permitir reservas para el próximo día
- Franjas de reserva, separadas por punto y coma

Ejemplo:

```text
08:00-10:00;10:00-12:00;12:00-14:00;14:00-16:00;16:00-18:00
```

### Pagos

Habilita únicamente los métodos que estén configurados:

- Yape: número, titular y QR.
- Plin: número, titular y QR.
- Transferencia: banco, tipo de cuenta, cuenta, CCI y titular.

Los QR se almacenan en el mismo bucket público configurado por el parche de imágenes.

### Mashico

Activa:

```text
Enviar fotografías de productos por WhatsApp
```

Cada producto debe tener su imagen registrada en:

```text
Dashboard → Productos → Editar
```

Cada combo debe tener imagen en:

```text
Dashboard → Promociones → Editar
```

## Pruebas recomendadas

En el simulador o WhatsApp escribe:

```text
menu
horario
bistec
reservar
```

Flujo de reserva:

```text
menu → 6 → categoría → producto → cantidad → finalizar → recojo/delivery → franja → pago → CONFIRMAR
```

Consulta de promociones:

```text
menu → 3
```

## Comprobar pedidos programados en Supabase

```sql
SELECT
    code,
    status,
    fulfillment_type,
    payment_method,
    scheduled_for,
    total,
    source,
    created_at
FROM customer_orders
ORDER BY created_at DESC;
```

## Comportamiento del stock

Cuando una reserva es confirmada, el stock se descuenta inmediatamente. Esto evita que el mismo producto sea vendido dos veces antes de la fecha programada. Si el pedido se cancela desde el dashboard, el stock se devuelve.

## Alcance actual

- WhatsApp envía una imagen por respuesta.
- Al seleccionar un corte se envía la imagen de ese producto.
- En promociones se envía la imagen del primer combo activo que tenga fotografía.
- El sistema muestra los datos de pago, pero no verifica automáticamente un comprobante de Yape, Plin o transferencia.
- Los feriados especiales todavía deben manejarse desactivando temporalmente el día o cambiando el horario desde Configuración.
