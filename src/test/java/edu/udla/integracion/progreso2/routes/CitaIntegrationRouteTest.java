package edu.udla.integracion.progreso2.routes;

import edu.udla.integracion.progreso2.model.CitaRequest;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@CamelSpringBootTest
@SpringBootTest(properties = {
        "app.routing.billing-endpoint=mock:billing",
        "app.routing.events-endpoint=mock:events",
        "app.routing.audit-endpoint=mock:audit"
})
class CitaIntegrationRouteTest {

    @Autowired
    private ProducerTemplate producerTemplate;

    @EndpointInject("mock:billing")
    private MockEndpoint billingMock;

    @EndpointInject("mock:events")
    private MockEndpoint eventsMock;

    @EndpointInject("mock:audit")
    private MockEndpoint auditMock;

    @Test
    void shouldSendValidAppointmentToAllTargets() throws Exception {
        billingMock.expectedMessageCount(1);
        billingMock.expectedBodiesReceived(
                "{\"idCita\":\"CITA-1001\",\"paciente\":\"Ana Torres\",\"especialidad\":\"Cardiologia\",\"valor\":45.5,\"tipoMensaje\":\"COMANDO_FACTURAR_CITA\"}"
        );

        eventsMock.expectedMessageCount(1);
        eventsMock.expectedBodiesReceived(
                "{\"idCita\":\"CITA-1001\",\"paciente\":\"Ana Torres\",\"correo\":\"ana.torres@email.com\",\"especialidad\":\"Cardiologia\",\"fechaCita\":\"2026-06-15\",\"sede\":\"Centro Norte\",\"tipoEvento\":\"CITA_CONFIRMADA\"}"
        );

        auditMock.expectedMessageCount(1);
        auditMock.expectedBodiesReceived(
                "CITA-1001,Ana Torres,ana.torres@email.com,Cardiologia,2026-06-15,Centro Norte,45.50"
                        + System.lineSeparator()
        );

        producerTemplate.sendBody("direct:processCita", new CitaRequest(
                "CITA-1001",
                "Ana Torres",
                "ana.torres@email.com",
                "Cardiologia",
                "2026-06-15",
                "Centro Norte",
                45.50
        ));

        MockEndpoint.assertIsSatisfied(billingMock, eventsMock, auditMock);
    }
}
