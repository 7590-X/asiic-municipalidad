import { decodeJwt, extractRoles, isTokenExpired, parseCurrentUser } from './jwt.util';

describe('jwt.util', () => {
  // Mock JWT con payload: {"sub":"user-123","email":"test@muni.gob.gt","preferred_username":"testuser","realm_access":{"roles":["Vecino","offline_access"]},"exp":253402300799}
  const createMockToken = (payload: object): string => {
    const header = btoa(JSON.stringify({ alg: 'RS256', typ: 'JWT' }));
    const body = btoa(JSON.stringify(payload)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
    return `${header}.${body}.mockSignature`;
  };

  it('debe decodificar correctamente un payload JWT', () => {
    const payload = { sub: '123', email: 'vecino@muni.gt' };
    const token = createMockToken(payload);

    const decoded = decodeJwt<typeof payload>(token);
    expect(decoded).toBeTruthy();
    expect(decoded?.sub).toBe('123');
    expect(decoded?.email).toBe('vecino@muni.gt');
  });

  it('debe retornar null ante un token malformado o vacío', () => {
    expect(decodeJwt('')).toBeNull();
    expect(decodeJwt('token-invalido')).toBeNull();
  });

  it('debe validar la expiración del token correctamente', () => {
    const expiredExp = Math.floor(Date.now() / 1000) - 100;
    const futureExp = Math.floor(Date.now() / 1000) + 3600;

    expect(isTokenExpired(expiredExp)).toBeTrue();
    expect(isTokenExpired(futureExp)).toBeFalse();
    expect(isTokenExpired(undefined)).toBeTrue();
  });

  it('debe extraer roles de realm_access y resource_access de Keycloak', () => {
    const claims = {
      sub: '456',
      realm_access: {
        roles: ['Vecino', 'default-roles']
      },
      resource_access: {
        'asiic-client': {
          roles: ['Editor', 'Vecino']
        }
      }
    };

    const roles = extractRoles(claims);
    expect(roles).toContain('Vecino');
    expect(roles).toContain('default-roles');
    expect(roles).toContain('Editor');
  });

  it('debe construir el CurrentUser con roles e identidad esperada', () => {
    const futureExp = Math.floor(Date.now() / 1000) + 3600;
    const token = createMockToken({
      sub: 'user-999',
      email: 'admin@muni.gt',
      preferred_username: 'admin',
      name: 'Admin ASIIC',
      realm_access: { roles: ['Administrador'] },
      exp: futureExp
    });

    const user = parseCurrentUser(token);
    expect(user).toBeTruthy();
    expect(user?.id).toBe('user-999');
    expect(user?.email).toBe('admin@muni.gt');
    expect(user?.name).toBe('Admin ASIIC');
    expect(user?.roles).toContain('Administrador');
  });
});
