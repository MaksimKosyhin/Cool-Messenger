package com.example.end;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EndApplication {
	//todo: configure openapi endpoints
	//todo: learn more about transactional annotation
	//todo: ?combine auth, registration and user service together; extract email verification into anoter service
	//todo: look for ways to ensure user can only edit his own profile (SecurityContextHolder?)
	public static void main(String[] args) {
		SpringApplication.run(EndApplication.class, args);
	}

}
