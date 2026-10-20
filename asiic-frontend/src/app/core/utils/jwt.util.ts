import { CurrentUser, KeycloakTokenClaims } from '../models/auth.model';

export function decodeJwt<T = any>(token: string): T | null {
  try {
    if (!token || typeof token !== 'string') {
      return null;
    }
    const parts = token.split('.');
    if (parts.length < 2) {
      return null;
    }

    let base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    while (base64.length % 4) {
      base64 += '=';
    }

    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );

    return JSON.parse(jsonPayload) as T;
  } catch (e) {
    console.error('Error decodificando token JWT:', e);
    return null;
  }
}

export function isTokenExpired(exp?: number): boolean {
  if (!exp) {
    return true;
  }
  // Se descuenta 5 segundos de margen de tolerancia para evitar condiciones de carrera
  const currentTime = Math.floor(Date.now() / 1000);
  return currentTime >= exp - 5;
}

export function extractRoles(claims: KeycloakTokenClaims): string[] {
  const rolesSet = new Set<string>();

  // 1. Keycloak Realm Access Roles
  if (claims.realm_access?.roles && Array.isArray(claims.realm_access.roles)) {
    for (const r of claims.realm_access.roles) {
      if (r) rolesSet.add(r);
    }
  }

  // 2. Keycloak Resource Access Roles
  if (claims.resource_access) {
    for (const clientKey of Object.keys(claims.resource_access)) {
      const clientRoles = claims.resource_access[clientKey]?.roles;
      if (Array.isArray(clientRoles)) {
        for (const r of clientRoles) {
          if (r) rolesSet.add(r);
        }
      }
    }
  }

  // 3. Direct roles array
  if (Array.isArray((claims as any).roles)) {
    for (const r of (claims as any).roles) {
      if (r) rolesSet.add(String(r));
    }
  }

  // 4. Authorities / Spring Security roles
  if (Array.isArray((claims as any).authorities)) {
    for (const r of (claims as any).authorities) {
      if (r) rolesSet.add(String(r));
    }
  }

  // 5. User roles / groups
  if (Array.isArray((claims as any).user_roles)) {
    for (const r of (claims as any).user_roles) {
      if (r) rolesSet.add(String(r));
    }
  }

  if (Array.isArray((claims as any).groups)) {
    for (const r of (claims as any).groups) {
      if (r) rolesSet.add(String(r).replace(/^\//, ''));
    }
  }

  // 6. Direct single role/type claim
  if ((claims as any).tipo_usuario) {
    rolesSet.add(String((claims as any).tipo_usuario));
  }
  if ((claims as any).role) {
    rolesSet.add(String((claims as any).role));
  }

  return Array.from(rolesSet);
}

export function parseCurrentUser(token: string): CurrentUser | null {
  const claims = decodeJwt<KeycloakTokenClaims>(token);
  if (!claims) {
    return null;
  }

  const roles = extractRoles(claims);
  const fullName =
    claims.name ||
    [claims.given_name, claims.family_name].filter(Boolean).join(' ') ||
    claims.preferred_username ||
    claims.email ||
    'Usuario';

  return {
    id: claims.sub,
    email: claims.email || '',
    username: claims.preferred_username || claims.email || '',
    name: fullName,
    roles,
    token,
    expiresAt: (claims.exp || 0) * 1000
  };
}
