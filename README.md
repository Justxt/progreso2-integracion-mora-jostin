# progreso2-integracion-mora-jostin

## 1. Nombre del estudiante
Jostin Mora

## 2. Descripcion breve de la solucion
Aplicacion desarrollada con Spring Boot, Apache Camel y RabbitMQ para registrar solicitudes de cita medica, validar datos, enviar un comando de facturacion, publicar un evento para multiples sistemas, generar un archivo CSV de auditoria y registrar errores de validacion o procesamiento.

## 3. Tecnologias utilizadas
- Java 17
- Spring Boot 4
- Apache Camel 4.19
- RabbitMQ
- Docker Compose
- Maven Wrapper
- JUnit 5

## 4. Instrucciones para levantar RabbitMQ
```bash
docker compose up -d
```

RabbitMQ Management:
- URL: `http://localhost:15672`
- Usuario: `guest`
- Clave: `guest`

## 5. Instrucciones para ejecutar la aplicacion
```bash
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## 6. Endpoint disponible
- `POST /api/citas`

## 7. Ejemplo de request valido
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

## 8. Ejemplo de request invalido
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

## 9. Explicacion breve de la integracion
- Point-to-Point: se aplica cuando la ruta Camel envia un comando de facturacion hacia `billing.queue`. Solo un consumidor debe procesar esa solicitud.
- Publish/Subscribe: se aplica cuando la ruta Camel publica el evento `CITA_CONFIRMADA` en el exchange `appointments.events`, permitiendo que `notifications.queue` y `analytics.queue` reciban el mismo evento.
- Transferencia de archivos: se aplica al generar `data/outbox/auditoria-citas.csv`, archivo que integra con el sistema legado de auditoria.
- Manejo de errores: las solicitudes invalidas o errores de procesamiento se registran en `data/errors/citas-rechazadas.log`.

## 10. Evidencia esperada para verificar el funcionamiento
- API ejecutandose correctamente.
- Request valido enviado por Postman, curl o Swagger.
- Respuesta exitosa `202 Accepted`.
- Mensaje de facturacion disponible en `billing.queue`.
- Evento distribuido a `notifications.queue` y `analytics.queue`.
- Archivo CSV generado con la cita procesada.
- Registro de error ante una solicitud invalida.
