package com.example.end;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EndApplication {

	//TODO: Improve tests; know about different testing annotations
	//todo: write tests for db
	//todo: confgure openspi endpoints
	public static void main(String[] args) {
		SpringApplication.run(EndApplication.class, args);
	}

}
