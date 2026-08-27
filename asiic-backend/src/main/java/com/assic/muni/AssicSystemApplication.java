package com.assic.muni;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import jakarta.annotation.PostConstruct;

@EnableFeignClients
@SpringBootApplication
public class AssicSystemApplication {

  public static void main(String[] args) {
    SpringApplication.run(AssicSystemApplication.class, args);
  }

  @PostConstruct
  public void setup() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/Guatemala"));
  }
}
