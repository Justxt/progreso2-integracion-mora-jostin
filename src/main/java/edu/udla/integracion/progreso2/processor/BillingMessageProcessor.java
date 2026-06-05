package edu.udla.integracion.progreso2.processor;

import edu.udla.integracion.progreso2.model.CitaRequest;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class BillingMessageProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        CitaRequest request = exchange.getIn().getBody(CitaRequest.class);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("idCita", request.idCita());
        message.put("paciente", request.paciente());
        message.put("especialidad", request.especialidad());
        message.put("valor", request.valor());
        message.put("tipoMensaje", "COMANDO_FACTURAR_CITA");

        exchange.getIn().setBody(message);
    }
}
