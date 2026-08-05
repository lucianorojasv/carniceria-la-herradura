# Implementación de WhatsApp

## Requisitos

- Cuenta comercial en Meta.
- Aplicación con producto WhatsApp.
- Número empresarial disponible.
- `Phone Number ID` y token de acceso.
- Dominio HTTPS público.

## Webhook

URL:

```text
https://DOMINIO/api/whatsapp/webhook
```

Token de verificación: el valor de `WHATSAPP_VERIFY_TOKEN`.

El controlador acepta la verificación GET y procesa mensajes de texto POST. Los tipos que todavía no son texto reciben el menú inicial.

## Salida

El servicio publica mensajes a:

```text
/{GRAPH_API_VERSION}/{PHONE_NUMBER_ID}/messages
```

Mantén `WHATSAPP_GRAPH_API_VERSION` actualizado según la versión configurada en la aplicación de Meta.

## Ventana y plantillas

La respuesta a una conversación iniciada por el cliente se maneja como atención. Los mensajes iniciados por la empresa fuera de la ventana permitida deben usar plantillas aprobadas. El presente código cubre recepción y respuesta; las campañas masivas deben configurarse después de obtener consentimiento.
