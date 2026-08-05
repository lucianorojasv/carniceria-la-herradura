# Supabase

La aplicación crea la base automáticamente con Flyway.

Archivos:

- `01_schema.sql`: estructura completa.
- `02_demo_data.sql`: usuario, catálogo y promociones de prueba.
- `03_verify.sql`: verificación.
- `04_reset_demo_data.sql`: elimina pedidos y clientes de prueba.

Los dos primeros archivos son copias de las migraciones incluidas en el backend. Solo ejecútalos manualmente si decides desactivar Flyway.

Para mantener la seguridad, las tablas tienen RLS activado y no incluyen políticas para las claves públicas `anon` o `authenticated`. El acceso se realiza únicamente mediante el backend Spring Boot.
