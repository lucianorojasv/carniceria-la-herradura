# Guía de despliegue en Vercel y Supabase

## 1. Crear el proyecto en Supabase

1. Ingresa a Supabase y crea un proyecto nuevo.
2. Elige una región cercana a los usuarios y, de ser posible, cercana a la región de ejecución de Vercel.
3. Guarda la contraseña de la base de datos.
4. Espera hasta que el proyecto figure como disponible.

No es obligatorio crear las tablas desde el panel. El backend incorpora Flyway y las genera en el primer arranque.

## 2. Obtener las conexiones correctas

En Supabase abre el botón **Connect**.

### Conexión de ejecución

Usa el **Transaction Pooler**, puerto `6543`, para las consultas normales de Spring Boot:

```text
DB_URL=jdbc:postgresql://aws-0-REGION.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0&tcpKeepAlive=true
DB_USER=postgres.PROJECT_REF
DB_PASSWORD=TU_PASSWORD
```

`prepareThreshold=0` evita prepared statements del servidor, que no son compatibles con el modo transaccional del pooler.

### Conexión de migraciones

Usa el **Session Pooler**, puerto `5432`, para Flyway:

```text
FLYWAY_URL=jdbc:postgresql://aws-0-REGION.pooler.supabase.com:5432/postgres?sslmode=require&tcpKeepAlive=true
FLYWAY_USER=postgres.PROJECT_REF
FLYWAY_PASSWORD=TU_PASSWORD
```

Esta conexión mantiene una sesión durante la migración y es más adecuada para los bloqueos usados por Flyway.

## 3. Variables del backend en Vercel

Registra estas variables para Production, Preview y Development:

```text
DB_URL
DB_USER
DB_PASSWORD
FLYWAY_URL
FLYWAY_USER
FLYWAY_PASSWORD
FLYWAY_ENABLED=true
JPA_DDL_AUTO=validate
DB_POOL_MAX_SIZE=2
DB_POOL_MIN_IDLE=0
DB_IDLE_TIMEOUT_MS=30000
DB_MAX_LIFETIME_MS=300000
JWT_SECRET
JWT_EXPIRATION_MINUTES=480
CORS_ALLOWED_ORIGIN_PATTERNS=https://*.vercel.app
```

Genera el secreto JWT con:

```bash
./scripts/generate-jwt-secret.sh
```

También puedes agregar el dominio final:

```text
CORS_ALLOWED_ORIGIN_PATTERNS=https://*.vercel.app,https://carnicerialaherradura.pe,https://www.carnicerialaherradura.pe
```

## 4. Subir el código a GitHub

Desde la carpeta del proyecto:

```bash
git init
git add .
git commit -m "Versión Vercel y Supabase"
git branch -M main
git remote add origin URL_DE_TU_REPOSITORIO
git push -u origin main
```

No subas el archivo `.env`.

## 5. Desplegar como un solo proyecto

El archivo raíz `vercel.json` define dos servicios:

- `frontend`: aplicación React.
- `backend`: API Spring Boot.

Las rutas se distribuyen así:

```text
/api/*       → backend
/actuator/*  → backend
/*           → frontend
```

En Vercel:

1. Selecciona **Add New → Project**.
2. Importa el repositorio.
3. Conserva la raíz del repositorio como **Root Directory**.
4. Agrega las variables de entorno.
5. Pulsa **Deploy**.

Vercel detectará los archivos `Dockerfile.vercel` de ambos servicios.

## 6. Primera inicialización

Después del despliegue abre:

```text
https://TU-PROYECTO.vercel.app/actuator/health
```

La primera solicitud puede tardar más porque el backend:

1. Inicia Java.
2. Se conecta a Supabase.
3. Ejecuta las migraciones Flyway.
4. Valida el modelo JPA.
5. Queda listo para recibir solicitudes.

Respuesta esperada:

```json
{"status":"UP"}
```

Luego abre:

```text
https://TU-PROYECTO.vercel.app/login
```

Credenciales iniciales:

```text
Usuario: admin
Contraseña: Herradura2026!
```

Cambia la contraseña inmediatamente.

## 7. Comprobar la base de datos

En el SQL Editor de Supabase ejecuta:

```text
supabase/03_verify.sql
```

Debe mostrar tablas, categorías, productos, zonas y promociones.

## 8. Alternativa si Vercel Services no aparece en tu cuenta

Crea dos proyectos separados.

### Backend

- Root Directory: `backend`
- Vercel detectará `backend/Dockerfile.vercel`.
- Agrega todas las variables de Supabase y seguridad.

### Frontend

- Root Directory: `frontend`
- Antes de desplegar, cambia `frontend/public/runtime-config.json`:

```json
{
  "apiUrl": "https://URL-DEL-BACKEND.vercel.app/api"
}
```

En CORS agrega la URL del frontend:

```text
CORS_ALLOWED_ORIGIN_PATTERNS=https://URL-DEL-FRONTEND.vercel.app
```

## 9. Configurar WhatsApp después

Cuando tengas Meta WhatsApp Cloud API, agrega:

```text
WHATSAPP_ENABLED=true
WHATSAPP_GRAPH_API_VERSION=v23.0
WHATSAPP_PHONE_NUMBER_ID=...
WHATSAPP_ACCESS_TOKEN=...
WHATSAPP_APP_SECRET=...
WHATSAPP_VERIFY_TOKEN=...
```

Webhook:

```text
https://TU-DOMINIO/api/whatsapp/webhook
```

## 10. n8n

n8n no se despliega dentro de esta configuración de Vercel. Usa una de estas opciones:

- n8n Cloud.
- VPS con Docker.
- Servidor propio.

El `docker-compose.yml` conserva n8n para pruebas locales.

## 11. Errores comunes

### `Connection refused`

Revisa host, región, puerto y contraseña. Para Vercel, el pooler de Supabase suele ser más compatible que la conexión directa.

### `prepared statement already exists` o errores similares

Confirma que el `DB_URL` de ejecución usa el puerto `6543` y contiene:

```text
prepareThreshold=0
```

### Fallo de Flyway

Confirma que `FLYWAY_URL` usa Session Pooler en el puerto `5432`.

### El frontend abre, pero las solicitudes devuelven 404

Comprueba que el proyecto se importó desde la raíz y que `vercel.json` fue detectado.

### Inicio lento

La primera solicitud puede activar una instancia nueva del contenedor. Esto es normal en un entorno que escala a cero.

## 12. Datos reales pendientes

Antes de publicar reemplaza:

- Dirección.
- Horario.
- Lista de productos.
- Precios.
- Stock.
- Zonas y tarifas de delivery.
- Métodos de pago.
- Fotografías.
- Promociones.
- Credenciales de WhatsApp.
