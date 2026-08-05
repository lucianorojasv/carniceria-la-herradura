# Cambios realizados para Vercel + Supabase

- Se agregó `vercel.json` con dos servicios: React y Spring Boot.
- Se crearon `Dockerfile.vercel` para frontend y backend.
- El backend escucha la variable `$PORT` de Vercel.
- PostgreSQL local fue reemplazable por Supabase mediante variables de entorno.
- Se separó la conexión runtime (Transaction Pooler 6543) de la conexión de migraciones (Session Pooler 5432).
- Se agregó Flyway con esquema y datos demostrativos automáticos.
- Hibernate quedó en modo `validate` para evitar cambios de estructura no controlados.
- Se agregó RLS sin políticas públicas a todas las tablas de negocio.
- Se redujo el pool de conexiones para un entorno autoscalable.
- Se desactivaron prepared statements del servidor en la conexión transaccional.
- Se agregó bloqueo pesimista para evitar doble descuento de stock.
- Se agregó configuración de API en `frontend/public/runtime-config.json`.
- Se agregó guía completa de despliegue y scripts de verificación.
