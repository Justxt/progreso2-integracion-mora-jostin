package edu.udla.integracion.progreso2.model;

public record CitaRequest(
        String idCita,
        String paciente,
        String correo,
        String especialidad,
        String fechaCita,
        String sede,
        Double valor
) {
}
