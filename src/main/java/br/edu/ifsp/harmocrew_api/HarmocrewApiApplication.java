package br.edu.ifsp.harmocrew_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HarmocrewApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HarmocrewApiApplication.class, args);
	}

}
