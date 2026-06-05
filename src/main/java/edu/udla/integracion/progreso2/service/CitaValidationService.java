package edu.udla.integracion.progreso2.service;

import edu.udla.integracion.progreso2.model.CitaRequest;
import org.springframework.stereotype.Service;

@Service
public class CitaValidationService {

    public void validate(CitaRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El payload de la cita es obligatorio");
        }

        requireText(request.idCita(), "idCita es obligatorio");
        requireText(request.paciente(), "paciente es obligatorio");
        requireText(request.correo(), "correo es obligatorio");
        requireText(request.especialidad(), "especialidad es obligatoria");
        requireText(request.fechaCita(), "fechaCita es obligatoria");
        requireText(request.sede(), "sede es obligatoria");

        if (request.valor() == null || request.valor() <= 0) {
            throw new IllegalArgumentException("El valor debe ser mayor a 0");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
