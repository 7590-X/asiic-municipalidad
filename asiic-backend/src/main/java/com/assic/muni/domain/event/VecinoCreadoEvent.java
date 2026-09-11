package com.assic.muni.domain.event;

import java.time.ZonedDateTime;

public record VecinoCreadoEvent(
  String userId, String email,String fullName
) {
}
