package edu.udla.integracion.progreso2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.udla.integracion.progreso2.model.CitaRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

@Service
public class ErrorLogService {

    private final Path errorFile;
    private final ObjectMapper objectMapper;

    public ErrorLogService(@Value("${app.error.file}") String errorFilePath, ObjectMapper objectMapper) {
        this.errorFile = Path.of(errorFilePath);
        this.objectMapper = objectMapper;
    }

    public void logValidationError(CitaRequest request, String reason) {
        writeLine(request != null ? request.idCita() : "N/A", reason, request);
    }

    public void logProcessingError(CitaRequest request, String reason) {
        writeLine(request != null ? request.idCita() : "N/A", reason, request);
    }

    private void writeLine(String idCita, String reason, Object payload) {
        String line = "%s | idCita=%s | motivo=%s | payload=%s%n".formatted(
                LocalDateTime.now(),
                idCita,
                reason,
                toJson(payload)
        );

        try {
            Files.createDirectories(errorFile.getParent());
            Files.writeString(
                    errorFile,
                    line,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo escribir el log de errores", exception);
        }
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            return String.valueOf(payload);
        }
    }
}
