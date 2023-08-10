package com.example.end;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EndApplication {
	//todo: configure openapi endpoints
	//todo: notify client when referenced entity is deleted
	//todo: refactor change email logic
	//todo: look for email sender services
	//todo: use logging
	//todo: find proper file storage solution
	//todo: ?map enums from request automatically
	//todo: separate project for testcontainers
	public static void main(String[] args) {
		SpringApplication.run(EndApplication.class, args);
	}

}
