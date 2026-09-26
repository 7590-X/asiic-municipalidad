package com.assic.muni.domain.event;

import java.time.Instant;

public record CuentaConfirmadaEvent(
        String fullName, Instant confirmationTime, String email) {

}
