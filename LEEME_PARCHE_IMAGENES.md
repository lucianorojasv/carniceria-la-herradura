# Parche v6 — Fotografías de productos

Este parche agrega al módulo **Productos**:

- Miniatura en la tabla de productos.
- Vista previa de la fotografía.
- Botón **Seleccionar imagen de mi PC**.
- Subida segura desde Spring Boot hacia Supabase Storage.
- Opción alternativa para pegar una URL.
- Validación JPG, PNG y WEBP, máximo 4 MB.

No reemplaza `SecurityConfig.java`, por lo que conserva el parche CORS aplicado anteriormente.

## 1. Crear el bucket en Supabase

En Supabase:

1. Storage.
2. New bucket.
3. Nombre: `product-images`.
4. Activar **Public bucket**.
5. Límite: 4 MB.
6. Tipos permitidos: `image/jpeg`, `image/png`, `image/webp`.

La subida se realiza desde el backend con una clave secreta; no se expone ninguna clave en Vercel ni en el navegador.

## 2. Variables en Render

En Render > carniceria-la-herradura-api > Environment, agrega:

```env
SUPABASE_URL=https://gkvavpmagtwlhsfalmoi.supabase.co
SUPABASE_SECRET_KEY=TU_CLAVE_SECRETA_DE_SUPABASE
SUPABASE_STORAGE_BUCKET=product-images
```

La clave se obtiene en Supabase > Settings > API Keys > Secret keys.
También funciona temporalmente la clave antigua `service_role` usando la variable:

```env
SUPABASE_SERVICE_ROLE_KEY=TU_SERVICE_ROLE
```

No coloques estas claves en Vercel, GitHub ni archivos del frontend.

## 3. Copiar los archivos

Copia las carpetas `backend` y `frontend` de este parche sobre las carpetas del proyecto. Acepta reemplazar los archivos existentes.

Archivos nuevos:

- `backend/src/main/java/pe/laherradura/controller/MediaController.java`
- `backend/src/main/java/pe/laherradura/service/MediaStorageService.java`

Archivos modificados:

- `frontend/src/pages/Products.jsx`
- `frontend/src/services/api.js`
- `frontend/src/styles.css`

## 4. Subir a GitHub

```powershell
git add backend/src/main/java/pe/laherradura/controller/MediaController.java
git add backend/src/main/java/pe/laherradura/service/MediaStorageService.java
git add frontend/src/pages/Products.jsx
git add frontend/src/services/api.js
git add frontend/src/styles.css
git commit -m "Agregar carga de imágenes de productos"
git push origin main
```

Render y Vercel desplegarán automáticamente.

## 5. Usar la función

1. Ingresa al dashboard.
2. Abre **Productos**.
3. Pulsa **Editar** o **Nuevo producto**.
4. Pulsa **Seleccionar imagen de mi PC**.
5. Espera la vista previa.
6. Pulsa **Guardar cambios**.

La URL de Supabase se guarda en la columna existente `products.image_url`, por lo que no hace falta ejecutar SQL adicional.

## 6. Cambiar portada y logo actuales

Mientras se agrega un administrador visual para portada y logo, reemplaza estos archivos en el proyecto:

- `frontend/public/cover.png` — portada recomendada 1920 × 700 px.
- `frontend/public/logo.png` — logo cuadrado PNG, preferiblemente transparente.

Después:

```powershell
git add frontend/public/cover.png frontend/public/logo.png
git commit -m "Actualizar portada y logo"
git push origin main
```
