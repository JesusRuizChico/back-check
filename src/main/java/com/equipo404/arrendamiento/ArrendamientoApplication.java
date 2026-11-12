package com.equipo404.arrendamiento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ArrendamientoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArrendamientoApplication.class, args);
	}

}
