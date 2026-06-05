package edu.udla.integracion.progreso2.processor;

import edu.udla.integracion.progreso2.model.CitaRequest;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CsvAuditProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        CitaRequest request = exchange.getIn().getBody(CitaRequest.class);
        String line = String.format(Locale.US, "%s,%s,%s,%s,%s,%s,%.2f%n",
                request.idCita(),
                request.paciente(),
                request.correo(),
                request.especialidad(),
                request.fechaCita(),
                request.sede(),
                request.valor()
        );
        exchange.getIn().setBody(line);
    }
}
