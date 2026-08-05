# Parche v4 — Java con ruta absoluta

El log de Vercel mostró:

    sh: 1: exec: java: not found

Este parche usa directamente:

    /opt/java/openjdk/bin/java

## Aplicación

1. Reemplazar `backend/Dockerfile.vercel`.
2. Ejecutar:

    git add backend/Dockerfile.vercel
    git commit -m "Usar ruta absoluta de Java en Vercel"
    git push origin main

3. Esperar el despliegue nuevo.
4. Probar `/actuator/health`.

En los logs debe aparecer:

    [BOOT] Verificando Java absoluto
    [BOOT] Iniciando Spring Boot en puerto 80

Si `/opt/java/openjdk/bin/java` no existe, el servicio de Vercel no está ejecutando la imagen final del Dockerfile y el backend deberá desplegarse como proyecto independiente o en otro host de contenedores.
