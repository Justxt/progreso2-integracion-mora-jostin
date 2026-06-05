package edu.udla.integracion.progreso2.controller;

import edu.udla.integracion.progreso2.model.ApiErrorResponse;
import edu.udla.integracion.progreso2.model.CitaRequest;
import edu.udla.integracion.progreso2.service.CitaValidationService;
import edu.udla.integracion.progreso2.service.ErrorLogService;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaValidationService validationService;
    private final ErrorLogService errorLogService;
    private final ProducerTemplate producerTemplate;

    public CitaController(
            CitaValidationService validationService,
            ErrorLogService errorLogService,
            ProducerTemplate producerTemplate
    ) {
        this.validationService = validationService;
        this.errorLogService = errorLogService;
        this.producerTemplate = producerTemplate;
    }

    @PostMapping
    public ResponseEntity<?> registrarCita(@RequestBody CitaRequest request) {
        try {
            validationService.validate(request);
            producerTemplate.sendBody("direct:processCita", request);

            return ResponseEntity.accepted().body(
                    Map.of(
                            "message", "Cita recibida y enviada al flujo de integracion",
                            "idCita", request.idCita()
                    )
            );
        } catch (IllegalArgumentException exception) {
            errorLogService.logValidationError(request, exception.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiErrorResponse("Solicitud invalida", exception.getMessage()));
        } catch (Exception exception) {
            errorLogService.logProcessingError(request, exception.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse("Error de procesamiento", exception.getMessage()));
        }
    }
}
