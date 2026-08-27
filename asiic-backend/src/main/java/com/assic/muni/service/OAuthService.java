package com.assic.muni.service;

import java.util.Map;

import com.assic.muni.dto.RegisterLoginCommand;
import com.assic.muni.dto.RegisterUserCommand;

public interface OAuthService {

  String register(RegisterUserCommand command);

  public Map<String, Object> login(RegisterLoginCommand command);




}
