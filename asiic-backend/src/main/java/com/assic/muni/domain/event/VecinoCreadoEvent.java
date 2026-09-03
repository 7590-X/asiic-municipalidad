package com.assic.muni.domain.event;

import org.springframework.modulith.NamedInterface;

import java.time.ZonedDateTime;

@NamedInterface
public record VecinoCreadoEvent(
  String email, String tempPassword, String message, ZonedDateTime zonedDateTime
) {
    
}
