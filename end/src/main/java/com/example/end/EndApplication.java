package com.example.end;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EndApplication {
	//todo: configure openapi endpoints
	//todo: notify client when referenced entity is deleted
	//todo: ?make component that handles roles and permissions logic
	//todo: learn more about mapstruct
	//todo: extract all search contact operations into separate service
	//todo: refactor EntityReference class
	public static void main(String[] args) {
		SpringApplication.run(EndApplication.class, args);
	}

}
