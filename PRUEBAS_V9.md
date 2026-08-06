# Pruebas del parche v9

## Validaciones realizadas durante la generación

### Frontend

- Se analizaron todos los archivos `.js` y `.jsx` con el parser/transpilador de TypeScript configurado para JSX.
- Resultado: **21 archivos revisados, 0 errores de sintaxis**.

### Backend

- Se compiló el subconjunto completo afectado por el parche con Java 21 y contratos mínimos de las dependencias externas.
- Incluyó entidades, DTO, repositorios, servicios de catálogo/pedidos/Mashico/ubicación y controladores públicos.
- Resultado: **compilación correcta**.

### Horarios

Prueba ejecutada:

- calcula el próximo día de atención
- acepta una franja configurada
- rechaza una hora no configurada

Resultado:

```text
BusinessHoursSmoke OK
```

## Limitaciones del entorno de generación

El entorno no pudo descargar dependencias desde npm ni Maven Central, por lo que no fue posible ejecutar aquí:

```text
npm run build
mvn test
```

Los archivos sí fueron validados sintácticamente y el backend afectado fue compilado con Java 21 mediante contratos de prueba. Debes ejecutar los builds reales en tu equipo o dejar que Vercel y Render los ejecuten al hacer push.

## Comandos recomendados en local

### Frontend

```powershell
cd frontend
npm install
npm run build
```

### Backend

```powershell
cd backend
mvn clean test
mvn clean package -DskipTests
```

## Pruebas funcionales

### Administración de local

1. Iniciar sesión como administrador.
2. Abrir `/locales`.
3. Registrar dirección y coordenadas.
4. Marcar el local como principal y visible.
5. Subir una fotografía de fachada.
6. Abrir `/ubicacion`.
7. Verificar mapa, fotografía y ruta.

### Pedido web

1. Abrir `/catalogo` sin iniciar sesión.
2. Agregar dos productos.
3. Cambiar cantidades.
4. Finalizar pedido.
5. Probar recojo y delivery.
6. Probar pedido para hoy dentro del horario.
7. Probar reserva en una franja válida.
8. Confirmar y copiar el código.

### Seguimiento

1. Abrir `/pedido`.
2. Ingresar código y celular correctos.
3. Verificar datos del pedido.
4. Probar celular incorrecto y confirmar que no expone información.

### Mashico

Enviar:

```text
ubicación
cómo llegar
7
```

Debe devolver dirección, horario, ruta y fotografía cuando esté configurada.
