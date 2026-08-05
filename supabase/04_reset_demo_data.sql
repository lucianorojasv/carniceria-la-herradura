-- CUIDADO: borra pedidos, clientes y conversaciones de prueba.
-- No ejecutar en producción sin una copia de seguridad.
TRUNCATE TABLE
    whatsapp_message_log,
    chat_sessions,
    order_items,
    customer_orders,
    customers
RESTART IDENTITY CASCADE;
