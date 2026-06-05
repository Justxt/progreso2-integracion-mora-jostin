package edu.udla.integracion.progreso2.config;

import org.apache.camel.CamelContext;
import org.apache.camel.component.springrabbit.SpringRabbitMQComponent;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelRabbitConfig {

    @Bean
    public CamelContextConfiguration rabbitComponentConfiguration(ConnectionFactory connectionFactory) {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
                SpringRabbitMQComponent component = new SpringRabbitMQComponent();
                component.setConnectionFactory(connectionFactory);
                camelContext.addComponent("spring-rabbitmq", component);
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                // No-op: the component only needs to be registered before routes start.
            }
        };
    }
}
