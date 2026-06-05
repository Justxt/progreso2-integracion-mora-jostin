package edu.udla.integracion.progreso2.model;

public record ApiErrorResponse(
        String error,
        String details
) {
}
