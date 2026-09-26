package com.assic.muni.application.port.out;

import com.assic.muni.application.port.out.dto.SimpleMail;

public interface EmailServicePort {

  void sendSimpleEmail(SimpleMail simpleMail);
}
