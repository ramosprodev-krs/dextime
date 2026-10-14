package com.dextime.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DextimeApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DextimeApiApplication.class, args);
	}

}
