package com.example.end.config;

import jakarta.mail.internet.MimeMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;
import java.util.Properties;

@Configuration
public class MailConfig {
    @Bean
    @Profile("dev")
    public JavaMailSender javaMailSender() {
        var mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername("maksimkosihyn.dev.test@gmail.com");
        mailSender.setPassword("ka7ds6sa");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    @Bean
    @Profile("test")
    public JavaMailSender MockMailSender() {
        return new MockMailSender();
    }

    public static class MockMailSender implements JavaMailSender{
        private String token;
        @Override
        public MimeMessage createMimeMessage() {
            return null;
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
            return null;
        }

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {

        }

        @Override
        public void send(MimeMessage... mimeMessages) throws MailException {

        }

        @Override
        public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {

        }

        @Override
        public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {

        }

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            var text = simpleMessage.getText();
            this.token = text.substring(text.indexOf("token=")).substring(6);
        }

        public String getToken() {
            return this.token;
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) throws MailException {

        }
    }
}
