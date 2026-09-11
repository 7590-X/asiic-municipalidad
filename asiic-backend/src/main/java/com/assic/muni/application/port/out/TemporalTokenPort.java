package com.assic.muni.application.port.out;

import com.assic.muni.application.enums.ETokenAction;

public interface TemporalTokenPort {

    String generateResetPasswordToken(String userId);

    String generateVerifyEmailToken(String userId);

    String validateAndExtractUserId(String token, ETokenAction expectedAction);

}
