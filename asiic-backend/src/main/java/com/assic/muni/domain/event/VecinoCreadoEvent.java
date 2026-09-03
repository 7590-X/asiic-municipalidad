package com.assic.muni.domain.event;

import java.time.ZonedDateTime;

public record VecinoCreadoEvent(
  String email, String tempPassword, String message, ZonedDateTime zonedDateTime
) {
    
}
