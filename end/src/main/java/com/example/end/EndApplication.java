package com.example.end;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EndApplication {
	//todo: configure openapi endpoints
	//todo: how to modify exception msg of validation
	//todo: write tests for UserController
	public static void main(String[] args) {
		SpringApplication.run(EndApplication.class, args);
	}

}
