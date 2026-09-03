package com.assic.muni.infrastructure.security;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private final JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();

  @Value("${keycloak.principalClaimName}")

  private final String principalClaimName;

  @Value("${keycloak.client}")
  private final String client;

  @Override
  public AbstractAuthenticationToken convert(Jwt source) {
    Collection<GrantedAuthority> authorities = Stream.concat(
        converter.convert(source).stream(),
        extractResourceRole(source).stream())
        .collect(Collectors.toSet());

    return new JwtAuthenticationToken(source, authorities, getPrincipalName(source));
  }

  private String getPrincipalName(Jwt jwt) {
    return jwt.getClaimAsString(principalClaimName);
  }

  private Collection<? extends GrantedAuthority> extractResourceRole(Jwt jwt) {
    Map<String, Object> resourceAccess;
    Map<String, Object> resource;
    Collection<String> resourceRoles;
    if (!jwt.hasClaim("resource_access")) {
      return Set.of();
    }
    resourceAccess = jwt.getClaimAsMap("resource_access");
    if (resourceAccess.get(client) == null) {
      return Set.of();
    }
    resource = (Map<String, Object>) resourceAccess.get(client);
    resourceRoles = (Collection<String>) resource.get("roles");
    return resourceRoles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toSet());
  }
}
