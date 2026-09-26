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

  if (claims.realm_access?.roles) {
    for (const r of claims.realm_access.roles) {
      rolesSet.add(r);
    }
  }

  if (claims.resource_access) {
    for (const clientKey of Object.keys(claims.resource_access)) {
      const clientRoles = claims.resource_access[clientKey]?.roles;
      if (Array.isArray(clientRoles)) {
        for (const r of clientRoles) {
          rolesSet.add(r);
        }
      }
    }
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
