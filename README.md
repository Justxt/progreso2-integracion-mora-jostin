# progreso2-integracion-mora-jostin

## Nombre del estudiante
Jostin Mora

## Descripcion breve de la solucion
Aplicacion Spring Boot con Apache Camel y RabbitMQ para registrar citas medicas, validar datos, enviar un mensaje a facturacion, publicar eventos para notificaciones y analitica, generar un archivo CSV y registrar errores.

## Tecnologias utilizadas
- Java 17
- Spring Boot
- Apache Camel 4.19
- RabbitMQ
- Docker Compose
- Maven Wrapper
- JUnit 5

## Instrucciones para levantar RabbitMQ
```bash
docker compose up -d
```

Panel RabbitMQ: `http://localhost:15672`
Usuario: `guest`
Clave: `guest`

## Instrucciones para ejecutar la aplicacion
```bash
.\mvnw.cmd spring-boot:run
```

## Endpoint disponible
`POST /api/citas`

## Ejemplo de request valido
```json
{
  "idCita": "CITA-1001",
  "paciente": "Ana Torres",
  "correo": "ana.torres@email.com",
  "especialidad": "Cardiologia",
  "fechaCita": "2026-06-15",
  "sede": "Centro Norte",
  "valor": 45.50
}
```

## Ejemplo de request invalido
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

## Explicacion breve
- Point-to-Point: se usa para enviar la solicitud de facturacion a `billing.queue`.
- Publish/Subscribe: se usa para publicar el evento de cita confirmada en `appointments.events` y distribuirlo a `notifications.queue` y `analytics.queue`.
- Transferencia de archivos: se usa para generar `data/outbox/auditoria-citas.csv`.
- Manejo de errores: se usa `data/errors/citas-rechazadas.log`.

## Evidencia esperada
- API ejecutandose correctamente.
- Request valido enviado por Postman, curl o Swagger.
- Respuesta exitosa `202 Accepted`.
- Mensaje de facturacion disponible en `billing.queue`.
- Evento distribuido a `notifications.queue` y `analytics.queue`.
- Archivo CSV generado con la cita procesada.
- Registro de error ante una solicitud invalida.
