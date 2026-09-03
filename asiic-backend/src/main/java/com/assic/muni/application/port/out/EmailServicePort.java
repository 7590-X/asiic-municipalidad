package com.assic.muni.application.port.out;

import com.assic.muni.application.port.out.dto.SimpleMail;
import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface EmailServicePort {

  void sendSimpleEmail(SimpleMail simpleMail);
}
