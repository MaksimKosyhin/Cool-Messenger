package com.example.end;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EndApplication {
	//todo: configure openapi endpoints
	//todo: notify client when referenced entity is deleted
	//todo: refactor change email logic
	//todo: rewrite tests; controller with mockito, repository with testcontainers
	//todo: look for email sender services
	//todo: use logging
	//todo: find proper file storage solution
	public static void main(String[] args) {
		SpringApplication.run(EndApplication.class, args);
	}

}
