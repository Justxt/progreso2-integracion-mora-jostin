package edu.udla.integracion.progreso2.processor;

import edu.udla.integracion.progreso2.model.CitaRequest;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AppointmentEventProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        CitaRequest request = exchange.getIn().getBody(CitaRequest.class);

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("idCita", request.idCita());
        event.put("paciente", request.paciente());
        event.put("correo", request.correo());
        event.put("especialidad", request.especialidad());
        event.put("fechaCita", request.fechaCita());
        event.put("sede", request.sede());
        event.put("tipoEvento", "CITA_CONFIRMADA");

        exchange.getIn().setBody(event);
    }
}
