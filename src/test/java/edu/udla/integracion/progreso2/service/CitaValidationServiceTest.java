package edu.udla.integracion.progreso2.service;

import edu.udla.integracion.progreso2.model.CitaRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CitaValidationServiceTest {

    private final CitaValidationService validationService = new CitaValidationService();

    @Test
    void shouldAcceptValidRequest() {
        CitaRequest request = new CitaRequest(
                "CITA-1001",
                "Ana Torres",
                "ana.torres@email.com",
                "Cardiologia",
                "2026-06-15",
                "Centro Norte",
                45.50
        );

        assertDoesNotThrow(() -> validationService.validate(request));
    }

    @Test
    void shouldRejectWhenValueIsZero() {
        CitaRequest request = new CitaRequest(
                "CITA-1002",
                "Luis Perez",
                "luis.perez@email.com",
                "Pediatria",
                "2026-06-16",
                "Centro Sur",
                0.0
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validate(request)
        );

        assertEquals("El valor debe ser mayor a 0", exception.getMessage());
    }

    @Test
    void shouldRejectWhenPatientIsBlank() {
        CitaRequest request = new CitaRequest(
                "CITA-1003",
                " ",
                "paciente@email.com",
                "Dermatologia",
                "2026-06-17",
                "Centro Norte",
                30.0
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validate(request)
        );

        assertEquals("paciente es obligatorio", exception.getMessage());
    }
}
