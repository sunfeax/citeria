package com.sunfeax.citeria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CiteriaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CiteriaApplication.class, args);
	}

}
