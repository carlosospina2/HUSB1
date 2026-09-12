package com.eventify.husb1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Eventify.
 *
 * @SpringBootApplication combina:
 *   @Configuration      → clase fuente de definiciones de Beans.
 *   @EnableAutoConfiguration → activa la autoconfiguración de Spring Boot.
 *   @ComponentScan      → escanea todos los paquetes bajo com.eventify.husb1
 *                         para registrar @Service, @Repository, @RestController.
 */
@SpringBootApplication
public class EventifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventifyApplication.class, args);
    }
}
