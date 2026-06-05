package edu.udla.integracion.progreso2.routes;

import edu.udla.integracion.progreso2.processor.AppointmentEventProcessor;
import edu.udla.integracion.progreso2.processor.BillingMessageProcessor;
import edu.udla.integracion.progreso2.processor.CsvAuditProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CitaIntegrationRoute extends RouteBuilder {

    private final BillingMessageProcessor billingMessageProcessor;
    private final AppointmentEventProcessor appointmentEventProcessor;
    private final CsvAuditProcessor csvAuditProcessor;
    private final String billingEndpoint;
    private final String eventsEndpoint;
    private final String auditEndpoint;

    public CitaIntegrationRoute(
            BillingMessageProcessor billingMessageProcessor,
            AppointmentEventProcessor appointmentEventProcessor,
            CsvAuditProcessor csvAuditProcessor,
            @Value("${app.routing.billing-endpoint}") String billingEndpoint,
            @Value("${app.routing.events-endpoint}") String eventsEndpoint,
            @Value("${app.routing.audit-endpoint}") String auditEndpoint
    ) {
        this.billingMessageProcessor = billingMessageProcessor;
        this.appointmentEventProcessor = appointmentEventProcessor;
        this.csvAuditProcessor = csvAuditProcessor;
        this.billingEndpoint = billingEndpoint;
        this.eventsEndpoint = eventsEndpoint;
        this.auditEndpoint = auditEndpoint;
    }

    @Override
    public void configure() {
        from("direct:processCita")
                .routeId("process-cita-route")
                .multicast()
                .stopOnException()
                .to("direct:billing", "direct:appointment-event", "direct:audit-csv");

        from("direct:billing")
                .routeId("billing-route")
                .process(billingMessageProcessor)
                .marshal().json()
                .to(billingEndpoint);

        from("direct:appointment-event")
                .routeId("appointment-event-route")
                .process(appointmentEventProcessor)
                .marshal().json()
                .to(eventsEndpoint);

        from("direct:audit-csv")
                .routeId("audit-csv-route")
                .process(csvAuditProcessor)
                .to(auditEndpoint);
    }
}
