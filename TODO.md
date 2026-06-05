# TODO - Guia para armar el informe final

## 1. Repositorio GitHub de la solucion

Repositorio publico:

`https://github.com/Justxt/progreso2-integracion-mora-jostin`

Antes de construir la solucion se reescribio la rama `main` para dejar el repositorio limpio y luego se implemento el proyecto desde cero.

## 2. Identificacion y analisis del problema de integracion

La organizacion Salud360 tenia un proceso manual para copiar la informacion de una cita medica entre varios sistemas. Ese flujo manual generaba cuatro problemas principales:

1. Errores de digitacion al copiar los datos entre sistemas.
2. Retrasos operativos porque cada sistema dependia de una accion humana.
3. Riesgo de duplicados en facturacion o notificaciones.
4. Falta de trazabilidad para saber que paso con cada cita.

Los sistemas que debian integrarse fueron:

- Sistema de Agenda Medica
- Sistema de Facturacion
- Sistema de Notificaciones
- Sistema de Analitica
- Sistema Legado de Auditoria

Los datos que debian circular entre sistemas fueron:

- `idCita`
- `paciente`
- `correo`
- `especialidad`
- `fechaCita`
- `sede`
- `valor`

Si la integracion se mantenia manual, los riesgos eran cobros incorrectos, mensajes no enviados, indicadores incompletos y ausencia de evidencia para auditoria.

## 3. Seleccion y justificacion de estilos y patrones de integracion

### API REST

Se utilizo una API REST para exponer el registro de citas mediante el endpoint `POST /api/citas`. Este estilo es adecuado porque permite recibir solicitudes de forma estandar desde Postman, curl o cualquier sistema cliente.

### Point-to-Point

Se utilizo el patron Point-to-Point para enviar la solicitud de facturacion a `billing.queue`. La justificacion es que la orden de cobro debe ser procesada por un unico sistema consumidor, evitando que varios servicios facturen la misma cita.

### Publish/Subscribe

Se utilizo Publish/Subscribe mediante el exchange `appointments.events` de tipo `fanout`. La justificacion es que el mismo evento de cita confirmada debe llegar a dos destinos diferentes: notificaciones y analitica.

### Transferencia de archivos

Se utilizo transferencia por archivo CSV porque el sistema legado de auditoria no expone API ni consume mensajeria. Por esa razon la unica forma de integracion compatible era generar `data/outbox/auditoria-citas.csv`.

## 4. Diseño tecnico de la solucion

La solucion fue implementada con Spring Boot, Apache Camel y RabbitMQ.

Flujo general:

1. El cliente envia una cita a `POST /api/citas`.
2. `CitaController` recibe el payload.
3. `CitaValidationService` valida que todos los campos obligatorios existan y que `valor > 0`.
4. Si la cita es valida, `ProducerTemplate` envia la cita a la ruta Camel `direct:processCita`.
5. `CitaIntegrationRoute` distribuye el procesamiento a tres subrutas:
   - `direct:billing`
   - `direct:appointment-event`
   - `direct:audit-csv`
6. La subruta de facturacion transforma la cita en un comando y lo envia a `billing.exchange` con routing key `billing.queue`.
7. La subruta de eventos transforma la cita en `CITA_CONFIRMADA` y la publica en `appointments.events`.
8. La subruta de auditoria convierte la cita en una linea CSV y la agrega al archivo de salida.
9. Si la solicitud es invalida o ocurre un error de procesamiento, `ErrorLogService` registra la evidencia en `data/errors/citas-rechazadas.log`.

Componentes principales:

- `src/main/java/edu/udla/integracion/progreso2/Progreso2Application.java`
- `src/main/java/edu/udla/integracion/progreso2/controller/CitaController.java`
- `src/main/java/edu/udla/integracion/progreso2/model/CitaRequest.java`
- `src/main/java/edu/udla/integracion/progreso2/routes/CitaIntegrationRoute.java`
- `src/main/java/edu/udla/integracion/progreso2/service/CitaValidationService.java`
- `src/main/java/edu/udla/integracion/progreso2/service/ErrorLogService.java`
- `src/main/java/edu/udla/integracion/progreso2/config/RabbitTopologyConfig.java`
- `src/main/java/edu/udla/integracion/progreso2/config/CamelRabbitConfig.java`

## 5. Implementacion realizada

### 5.1 API REST y validaciones

Se implemento el endpoint:

`POST /api/citas`

Validaciones aplicadas:

- `idCita` obligatorio
- `paciente` obligatorio
- `correo` obligatorio
- `especialidad` obligatoria
- `fechaCita` obligatoria
- `sede` obligatoria
- `valor` mayor a 0

Comportamiento:

- Si la cita es valida, la API responde `202 Accepted`.
- Si la cita es invalida, la API responde `400 Bad Request`.
- Si ocurre un error de integracion, la API responde `500 Internal Server Error`.

### 5.2 Rutas Apache Camel

Se implemento `CitaIntegrationRoute` con las siguientes rutas:

- `direct:processCita`
- `direct:billing`
- `direct:appointment-event`
- `direct:audit-csv`

La ruta principal usa `multicast()` para enviar la misma cita a tres destinos de integracion.

### 5.3 Mensajeria RabbitMQ

Se implementaron dos estilos:

- Point-to-Point:
  - Exchange: `billing.exchange`
  - Queue: `billing.queue`
  - Mensaje: `COMANDO_FACTURAR_CITA`

- Publish/Subscribe:
  - Exchange: `appointments.events`
  - Tipo: `fanout`
  - Queues: `notifications.queue`, `analytics.queue`
  - Evento: `CITA_CONFIRMADA`

Adicionalmente se configuro la topologia RabbitMQ mediante beans Spring AMQP para que exchanges, colas y bindings existan automaticamente al iniciar la app.

### 5.4 CSV y manejo de errores

Archivo CSV generado:

`data/outbox/auditoria-citas.csv`

Archivo de errores:

`data/errors/citas-rechazadas.log`

Cada error registrado contiene:

- fecha y hora
- `idCita`
- motivo
- payload recibido

## 6. Estructura final del proyecto

La estructura principal generada fue:

```text
progreso2-integracion-mora-jostin/
  README.md
  TODO.md
  docker-compose.yml
  pom.xml
  mvnw
  mvnw.cmd
  src/
    main/
      java/
        edu/udla/integracion/progreso2/
          Progreso2Application.java
          config/
          controller/
          model/
          processor/
          routes/
          service/
      resources/
        application.properties
    test/
      java/
        edu/udla/integracion/progreso2/
          controller/
          routes/
          service/
  data/
    outbox/
      auditoria-citas.csv
    errors/
      citas-rechazadas.log
  docs/
    capturas/
```

## 7. Evidencia tecnica real obtenida

### 7.1 Pruebas automatizadas

Se ejecuto:

```powershell
.\mvnw.cmd test
```

Resultado obtenido:

- `BUILD SUCCESS`
- `Tests run: 7, Failures: 0, Errors: 0, Skipped: 0`

### 7.2 Ejecucion de RabbitMQ

Se ejecuto:

```powershell
docker compose up -d
```

Resultado verificado:

- Contenedor `progreso2-rabbitmq` levantado
- RabbitMQ Management accesible en `http://localhost:15672`

### 7.3 Request valido

Request probado:

```json
{
  "idCita": "CITA-3001",
  "paciente": "Carlos Mena",
  "correo": "carlos.mena@email.com",
  "especialidad": "Traumatologia",
  "fechaCita": "2026-06-18",
  "sede": "Centro Sur",
  "valor": 60.00
}
```

Respuesta real obtenida:

- HTTP `202`

```json
{
  "message": "Cita recibida y enviada al flujo de integracion",
  "idCita": "CITA-3001"
}
```

### 7.4 Request invalido

Request probado:

```json
{
  "idCita": "",
  "paciente": "Ana Torres",
  "correo": "ana.torres@email.com",
  "especialidad": "Cardiologia",
  "fechaCita": "2026-06-15",
  "sede": "Centro Norte",
  "valor": 45.50
}
```

Respuesta real obtenida:

- HTTP `400`

```json
{
  "error": "Solicitud invalida",
  "details": "idCita es obligatorio"
}
```

### 7.5 Evidencia en RabbitMQ

Verificacion obtenida desde la API de administracion de RabbitMQ:

- `billing.queue`: contiene mensajes listos (`messages = 1` o mas segun pruebas)
- `notifications.queue`: contiene mensajes listos
- `analytics.queue`: contiene mensajes listos
- `appointments.events`: exchange de tipo `fanout`

Dato real observado en la verificacion:

- `appointments.events` mostro `publish_in = 2` y `publish_out = 4` durante las pruebas realizadas.

### 7.6 Evidencia del CSV

Contenido actual observado:

```text
idCita,paciente,correo,especialidad,fechaCita,sede,valor
CITA-1001,Ana Torres,ana.torres@email.com,Cardiologia,2026-06-15,Centro Norte,45.50
CITA-3001,Carlos Mena,carlos.mena@email.com,Traumatologia,2026-06-18,Centro Sur,60.00
```

### 7.7 Evidencia del log de errores

Contenido observado:

```text
2026-06-04T19:52:28.298291400 | idCita= | motivo=idCita es obligatorio | payload={"idCita":"","paciente":"Ana Torres","correo":"ana.torres@email.com","especialidad":"Cardiologia","fechaCita":"2026-06-15","sede":"Centro Norte","valor":45.5}
```

## 8. Reflexion tecnica final

### 8.1 Por que no seria suficiente resolver todo unicamente con archivos

No seria suficiente porque los archivos no permiten integracion en tiempo real ni distribucion inmediata de mensajes. Ademas, no son adecuados para escenarios donde un solo mensaje debe llegar a sistemas diferentes con baja latencia.

### 8.2 Por que no seria adecuado enviar la facturacion por Publish/Subscribe

No seria adecuado porque la facturacion debe ser procesada por un unico consumidor. Si se usara Publish/Subscribe, varios consumidores podrian reaccionar al mismo mensaje y producir cobros duplicados.

### 8.3 Que ventaja aporta RabbitMQ frente a una integracion directa API contra API

RabbitMQ desacopla los sistemas, permite colas, reintentos, mejor tolerancia a fallos y entrega asincronica. Con API directa, si el sistema destino falla en ese momento, el flujo completo podria romperse.

### 8.4 Que mejoraria si esta solucion tuviera que operar en produccion

En produccion se deberian agregar autenticacion, validaciones mas robustas, observabilidad centralizada, reintentos controlados, dead-letter queues, trazas distribuidas, pruebas de integracion completas, persistencia de auditoria en base de datos y despliegue automatizado.

## 9. Resumen breve de lo que se hizo

1. Se reseteo el repositorio GitHub y se reconstruyo el proyecto desde cero.
2. Se genero un proyecto Spring Boot con Maven Wrapper.
3. Se configuro Apache Camel 4.19 y RabbitMQ.
4. Se implemento la API REST `POST /api/citas`.
5. Se implementaron validaciones funcionales.
6. Se implemento la ruta Camel de orquestacion.
7. Se implemento Point-to-Point para facturacion.
8. Se implemento Publish/Subscribe para notificaciones y analitica.
9. Se genero el CSV de auditoria.
10. Se implemento el log de errores.
11. Se escribieron pruebas automatizadas.
12. Se verifico el flujo con RabbitMQ real, CSV real y errores reales.
