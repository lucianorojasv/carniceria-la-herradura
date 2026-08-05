# Parche v7 — Mashico + fotografías de promociones

Este parche realiza dos cambios:

1. Cambia el nombre del asistente automático de **Héctor** a **Mashico** en:
   - Mensaje inicial de WhatsApp.
   - Nota del pedido generado por el bot.
   - Recomendaciones con OpenAI.
   - Simulador del módulo Asistente.

2. Agrega fotografías para promociones y combos:
   - Botón **Seleccionar imagen de mi PC**.
   - Vista previa en el formulario.
   - Imagen en la tarjeta administrativa.
   - Imagen en el catálogo público del cliente.
   - Opción alternativa para pegar una URL.
   - Formatos JPG, PNG y WEBP; máximo 4 MB.

## Requisitos

El parche usa la misma configuración de Supabase Storage empleada para productos:

```env
SUPABASE_URL=https://gkvavpmagtwlhsfalmoi.supabase.co
SUPABASE_SECRET_KEY=TU_CLAVE_SECRETA
SUPABASE_STORAGE_BUCKET=product-images
```

El bucket debe ser público. Las imágenes de promociones se guardan dentro de:

```text
product-images/promotions/
```

## 1. Copiar el parche

Copia las carpetas `backend`, `frontend` y `supabase` encima de la carpeta principal del proyecto `carniceria-la-herradura`.

Cuando Windows pregunte, selecciona **Reemplazar los archivos en el destino**.

Este parche no reemplaza `SecurityConfig.java`, por lo que conserva la corrección CORS que ya está funcionando.

## 2. Base de datos

La versión actual del proyecto ya tiene `promotions.image_url`. El archivo siguiente solo lo asegura y se puede ejecutar sin riesgo:

```text
supabase/05_ensure_promotion_images.sql
```

En Supabase:

```text
SQL Editor → New query → pegar el contenido → Run
```

Si la columna ya existe, PostgreSQL no realizará cambios.

## 3. Subir a GitHub

Desde PowerShell, dentro de la carpeta principal del proyecto:

```powershell
git status
git add backend/src/main/java/pe/laherradura/controller/MediaController.java
git add backend/src/main/java/pe/laherradura/service/MediaStorageService.java
git add backend/src/main/java/pe/laherradura/service/ChatbotService.java
git add backend/src/main/java/pe/laherradura/service/OpenAiRecommendationService.java
git add frontend/src/pages/Assistant.jsx
git add frontend/src/pages/Promotions.jsx
git add frontend/src/pages/PublicCatalog.jsx
git add frontend/src/services/api.js
git add frontend/src/styles.css
git add supabase/05_ensure_promotion_images.sql
git commit -m "Cambiar asistente a Mashico y agregar imágenes en promociones"
git push origin main
```

Render actualizará el backend y Vercel actualizará el frontend automáticamente.

## 4. Probar Mashico

Entra a:

```text
https://carniceria-la-herradura.vercel.app/asistente
```

Escribe:

```text
menu
```

Debe responder con:

```text
Soy Mashico, asistente de Carnicería La Herradura.
```

## 5. Agregar una imagen a una promoción

1. Entra al dashboard.
2. Abre **Promociones**.
3. Pulsa **Editar** o **Nueva promoción**.
4. Pulsa **Seleccionar imagen de mi PC**.
5. Espera la vista previa.
6. Pulsa **Guardar cambios**.
7. Abre el catálogo público.

La fotografía se verá en:

```text
https://carniceria-la-herradura.vercel.app/catalogo
```

## 6. Comprobación rápida

El endpoint del catálogo debe devolver la URL:

```text
https://carniceria-la-herradura-api.onrender.com/api/public/catalog
```

Busca en la promoción:

```json
"imageUrl": "https://...supabase.co/storage/v1/object/public/product-images/promotions/..."
```

## Nota

No coloques `SUPABASE_SECRET_KEY` en GitHub ni en Vercel. Debe permanecer únicamente en las variables protegidas de Render.
