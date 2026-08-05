# Carnicería La Herradura — Vercel + Supabase

Versión del sistema preparada para una prueba en la nube con:

- **Frontend:** React + Vite.
- **Backend:** Spring Boot 3.5 + Java 21.
- **Base de datos:** PostgreSQL administrado por Supabase.
- **Migraciones:** Flyway, ejecutadas automáticamente al iniciar el backend.
- **Despliegue:** Vercel Services con dos contenedores: frontend y backend.
- **WhatsApp:** webhook y agente Héctor preparados; requieren credenciales reales de Meta.
- **n8n:** disponible para desarrollo local, pero debe alojarse aparte de Vercel.

## Acceso de demostración

- Usuario: `admin`
- Contraseña temporal: `Herradura2026!`

Cámbiala inmediatamente desde **Configuración → Seguridad**.

## Estructura

```text
.
├── vercel.json                 # Enruta /api al backend y el resto al frontend
├── frontend/
│   ├── Dockerfile.vercel
│   ├── public/runtime-config.json
│   └── src/
├── backend/
│   ├── Dockerfile.vercel
│   ├── pom.xml
│   └── src/main/resources/db/migration/
├── supabase/
│   ├── 01_schema.sql
│   ├── 02_demo_data.sql
│   └── 03_verify.sql
└── docs/DEPLOY_VERCEL_SUPABASE.md
```

## Despliegue rápido

1. Crea un proyecto en Supabase y guarda la contraseña de PostgreSQL.
2. En **Connect**, copia las conexiones del **Transaction Pooler** y del **Session Pooler**.
3. Sube esta carpeta a un repositorio de GitHub.
4. Importa el repositorio completo en Vercel.
5. Agrega las variables indicadas en `.env.example`.
6. Despliega.
7. Abre `/actuator/health` para comprobar el backend.
8. Ingresa en `/login` con el usuario de demostración.

La primera inicialización crea automáticamente las tablas y los datos de prueba mediante Flyway.

Consulta la guía completa:

```text
docs/DEPLOY_VERCEL_SUPABASE.md
```

## Desarrollo local

```bash
cp .env.example .env
docker compose up -d --build
```

Abrir:

```text
Aplicación: http://localhost
API: http://localhost:8080
Salud: http://localhost:8080/actuator/health
```

Para iniciar también n8n:

```bash
docker compose --profile automation up -d --build
```

## Seguridad aplicada

- JWT para el panel administrativo.
- Contraseñas BCrypt.
- CORS configurable.
- RLS activado en las tablas de negocio de Supabase sin políticas públicas.
- Pool de conexiones reducido para un entorno autoscalable.
- Bloqueo pesimista al descontar stock.
- Migraciones versionadas.
- No se incluyen credenciales reales en el código.

## Nota sobre imágenes

El formulario actual guarda una **URL de imagen**. Para producción, sube las fotografías a Supabase Storage y pega la URL pública o firmada en el producto. No conviene enviar archivos grandes a través del backend de Vercel.
