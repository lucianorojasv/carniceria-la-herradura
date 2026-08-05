-- Verificación rápida después de crear la base de datos.
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN (
    'app_users','business_settings','categories','products','customers',
    'delivery_zones','promotions','customer_orders','order_items',
    'chat_sessions','whatsapp_message_log','flyway_schema_history'
  )
ORDER BY table_name;

SELECT
    (SELECT COUNT(*) FROM app_users) AS usuarios,
    (SELECT COUNT(*) FROM categories) AS categorias,
    (SELECT COUNT(*) FROM products) AS productos,
    (SELECT COUNT(*) FROM delivery_zones) AS zonas_delivery,
    (SELECT COUNT(*) FROM promotions) AS promociones;
