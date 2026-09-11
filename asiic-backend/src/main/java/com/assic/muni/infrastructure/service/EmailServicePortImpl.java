package com.assic.muni.infrastructure.service;

import com.assic.muni.application.port.out.EmailServicePort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.assic.muni.application.port.out.dto.SimpleMail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServicePortImpl implements EmailServicePort {

    private final JavaMailSender javaMailServer;

    /**
     * Variable de entorno que contiene el correo del remitente.
     */
    @Value("${spring.mail.from}")
    public String from;

    @Async
    @Override
    public void sendSimpleEmail(SimpleMail simpleMail) {
        try {
            MimeMessage message = javaMailServer.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(from);
            helper.setTo(simpleMail.destination());
            helper.setSubject(simpleMail.subject());
            helper.setText(simpleMail.htmlBody(), true);
            javaMailServer.send(message);
        } catch (MailException | MessagingException ex) {
            log.error("[ERROR_SEND_EMAIL]", ex);
        }
    }
}