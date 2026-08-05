# API principal

Base: `/api`

| Método | Ruta | Función |
|---|---|---|
| POST | `/auth/login` | Iniciar sesión |
| GET | `/public/catalog` | Catálogo público |
| GET/POST/PUT | `/products` | Productos |
| GET/POST/PUT | `/categories` | Categorías |
| GET/POST/PUT | `/customers` | Clientes |
| GET/POST | `/orders` | Listar y crear pedidos |
| PATCH | `/orders/{id}/status` | Cambiar estado |
| GET/POST/PUT | `/delivery-zones` | Zonas y tarifas |
| GET/POST/PUT | `/promotions` | Promociones |
| POST | `/chat/message` | Probar agente |
| GET/POST | `/whatsapp/webhook` | Meta webhook |
| GET/PUT | `/settings` | Datos del negocio |
| GET | `/dashboard` | Indicadores |

Salvo login, catálogo público y webhook, las rutas requieren `Authorization: Bearer <token>`.
