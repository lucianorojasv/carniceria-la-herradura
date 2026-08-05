# Guía del operador

1. Al iniciar el día, revisa stock y precios.
2. Observa pedidos `PENDING` y confirma disponibilidad.
3. Cambia el estado: `CONFIRMED` → `PREPARING` → `READY`.
4. Para delivery usa `OUT_FOR_DELIVERY` y finalmente `DELIVERED`.
5. Al cancelar, el sistema devuelve las cantidades al stock.
6. Los pedidos derivados por el bot deben atenderse desde WhatsApp Manager o el canal conectado.
7. No marques pago como recibido sin comprobar Yape, Plin, transferencia o efectivo.
8. Revisa las alertas de stock bajo en el dashboard.
