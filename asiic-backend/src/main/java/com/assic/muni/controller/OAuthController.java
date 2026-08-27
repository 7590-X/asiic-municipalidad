package com.assic.muni.controller;

import java.time.ZonedDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.assic.muni.dto.APIResponse;
import com.assic.muni.dto.RegisterLoginCommand;
import com.assic.muni.service.OAuthService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth")
public class OAuthController {

  private final OAuthService oAuthService;

  public ResponseEntity<APIResponse<Map<String, Object>>> login(@RequestBody RegisterLoginCommand command) {

    Map<String, Object> authentication = oAuthService.login(command);

    return ResponseEntity.ok(new APIResponse<Map<String, Object>>(
        200,
        "AUTHENTICATED",
        ZonedDateTime.now(),
        "Autenticación Exitosa",
        authentication));
  }
}
