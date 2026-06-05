package edu.udla.integracion.progreso2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.udla.integracion.progreso2.model.CitaRequest;
import edu.udla.integracion.progreso2.service.CitaValidationService;
import edu.udla.integracion.progreso2.service.ErrorLogService;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CitaControllerTest {

    private final CitaValidationService validationService = mock(CitaValidationService.class);
    private final ErrorLogService errorLogService = mock(ErrorLogService.class);
    private final ProducerTemplate producerTemplate = mock(ProducerTemplate.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CitaController controller = new CitaController(validationService, errorLogService, producerTemplate);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldAcceptValidRequest() throws Exception {
        CitaRequest request = new CitaRequest(
                "CITA-1001",
                "Ana Torres",
                "ana.torres@email.com",
                "Cardiologia",
                "2026-06-15",
                "Centro Norte",
                45.50
        );

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Cita recibida y enviada al flujo de integracion"))
                .andExpect(jsonPath("$.idCita").value("CITA-1001"));

        verify(validationService).validate(request);
        verify(producerTemplate).sendBody("direct:processCita", request);
    }

    @Test
    void shouldRejectInvalidRequest() throws Exception {
        CitaRequest request = new CitaRequest(
                "",
                "Ana Torres",
                "ana.torres@email.com",
                "Cardiologia",
                "2026-06-15",
                "Centro Norte",
                45.50
        );

        doThrow(new IllegalArgumentException("idCita es obligatorio"))
                .when(validationService).validate(any(CitaRequest.class));

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Solicitud invalida"))
                .andExpect(jsonPath("$.details").value("idCita es obligatorio"));

        verify(errorLogService).logValidationError(request, "idCita es obligatorio");
    }

    @Test
    void shouldReturnProcessingErrorWhenRouteFails() throws Exception {
        CitaRequest request = new CitaRequest(
                "CITA-1005",
                "Maria Lopez",
                "maria.lopez@email.com",
                "Neurologia",
                "2026-06-20",
                "Centro Norte",
                50.0
        );

        doThrow(new IllegalStateException("RabbitMQ no disponible"))
                .when(producerTemplate).sendBody("direct:processCita", request);

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error de procesamiento"))
                .andExpect(jsonPath("$.details").value("RabbitMQ no disponible"));

        verify(errorLogService).logProcessingError(request, "RabbitMQ no disponible");
    }
}
