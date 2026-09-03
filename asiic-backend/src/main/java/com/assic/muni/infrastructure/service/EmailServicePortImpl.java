package com.assic.muni.infrastructure.service;

import com.assic.muni.application.port.out.EmailServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.assic.muni.application.port.out.dto.SimpleMail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(from);
      message.setTo(simpleMail.destination());
      message.setSubject(simpleMail.subject());
      message.setText(simpleMail.htmlBody());
      javaMailServer.send(message);
    } catch (MailException ex) {
      log.error("No se pudo efectuar el envió del correo a {}: {}", simpleMail.destination(), simpleMail.htmlBody(), ex);
    }
  }
}