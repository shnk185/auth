package org.ngs.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${service.email.address}")
    private String serviceEmailAddress;

    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        log.info(serviceEmailAddress);
        message.setFrom(serviceEmailAddress);
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
    }
}
