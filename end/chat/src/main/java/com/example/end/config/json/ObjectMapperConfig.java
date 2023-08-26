package com.example.end.config.json;

import com.example.end.config.json.serializer.LocalDateTimeDeserializer;
import com.example.end.config.json.serializer.LocalDateTimeSerializer;
import com.example.end.config.json.serializer.ObjectIdDeserializer;
import com.example.end.config.json.serializer.ObjectIdSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class ObjectMapperConfig {
    @Bean
    public ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();

        var simpleModule = new SimpleModule();
        simpleModule.addSerializer(ObjectId.class, new ObjectIdSerializer());
        simpleModule.addDeserializer(ObjectId.class, new ObjectIdDeserializer());
        objectMapper.registerModule(simpleModule);

        var javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        objectMapper.registerModule(javaTimeModule);

        return objectMapper;
    }
}
