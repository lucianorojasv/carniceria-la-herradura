# Parche Vercel Backend v2

## Archivos que debes reemplazar/agregar en GitHub

1. Reemplaza `backend/Dockerfile.vercel`.
2. Agrega `backend/vercel-entrypoint.sh`.
3. No cambies `vercel.json`.

## Variables recomendadas en Vercel

```env
DB_URL=jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require&tcpKeepAlive=true
DB_USER=postgres.gkvavpmagtwlhsfalmoi
DB_PASSWORD=TU_PASSWORD_REAL
FLYWAY_ENABLED=false
JPA_DDL_AUTO=none
DB_POOL_MAX_SIZE=2
DB_POOL_MIN_IDLE=0
DB_CONNECTION_TIMEOUT_MS=10000
DB_IDLE_TIMEOUT_MS=30000
DB_MAX_LIFETIME_MS=300000
PORT=80
JWT_SECRET=TU_SECRETO_GENERADO
JWT_EXPIRATION_MINUTES=480
CORS_ALLOWED_ORIGIN_PATTERNS=https://*.vercel.app
```

Las tablas ya fueron creadas manualmente, por eso se usa `FLYWAY_ENABLED=false` y `JPA_DDL_AUTO=none` durante la prueba.

## Después

Haz commit y espera el despliegue automático, o ejecuta Redeploy sin reutilizar la caché. Revisa los logs del servicio `backend`. Ahora deben aparecer líneas `[BOOT]` y `[OK]` antes del inicio de Spring Boot.
