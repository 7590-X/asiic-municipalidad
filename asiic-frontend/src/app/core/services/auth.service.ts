import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { AuthLoginResponse, CurrentUser, LoginRequest } from '../models/auth.model';
import { isTokenExpired, parseCurrentUser } from '../utils/jwt.util';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly TOKEN_KEY = 'asiic_auth_token';
  private readonly REFRESH_TOKEN_KEY = 'asiic_refresh_token';
  private readonly REMEMBER_KEY = 'asiic_remember_session';

  // Estado reactivo con Signals de Angular 19
  readonly currentUser = signal<CurrentUser | null>(this.loadStoredUser());

  readonly isAuthenticated = computed(() => {
    const user = this.currentUser();
    if (!user) {
      return false;
    }
    return !isTokenExpired(Math.floor(user.expiresAt / 1000));
  });
  readonly userRoles = computed(() => this.currentUser()?.roles ?? []);

  /**
   * Autenticación contra el endpoint de Keycloak /api/v1/asiic/auth/login
   */
  login(credentials: LoginRequest): Observable<AuthLoginResponse> {
    const payload = {
      username: credentials.username.trim(),
      password: credentials.password
    };

    return this.http.post<AuthLoginResponse>(`${environment.apiBaseUrl}/auth/login`, payload).pipe(
      tap((response: any) => {
        const body = response?.payload || response;
        const token = body?.access_token || body?.accessToken;

        if (token) {
          const user = parseCurrentUser(token);

          if (user) {
            this.saveSession(token, credentials.recordar ?? false);
            this.currentUser.set(user);
            this.redirectToDashboard(user.roles);
          } else {
            console.error('[AuthService] No se pudo parsear el usuario desde el token:', token);
          }
        } else {
          console.error('[AuthService] No se encontró access_token en la respuesta:', response);
        }
      })
    );
  }

  /**
   * Confirmación de cuenta de vecino
   */
  confirmarCuenta(body: { token: string; password: string }): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${environment.apiBaseUrl}/auth/confirmar`, body);
  }

  /**
   * Cierre de sesión y limpieza de credenciales
   */
  logout(): void {
    this.clearStorage();
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  /**
   * Retorna el token JWT en memoria o almacenamiento
   */
  getToken(): string | null {
    const active = this.currentUser()?.token;
    if (active) {
      return active;
    }
    return sessionStorage.getItem(this.TOKEN_KEY) || localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Verifica si el usuario tiene un rol específico
   */
  hasRole(role: string): boolean {
    const roles = this.userRoles();
    return roles.some((r) => r.toLowerCase() === role.toLowerCase());
  }

  /**
   * Verifica si el usuario tiene al menos uno de los roles solicitados
   */
  hasAnyRole(roles: string[]): boolean {
    return roles.some((r) => this.hasRole(r));
  }

  /**
   * Retorna el rol principal del usuario (compatibilidad hacia atrás)
   */
  getRole(): string | null {
    const roles = this.userRoles();
    if (this.isAgenteCampo()) {
      return 'Agente de Campo';
    }
    if (this.isAnalista()) {
      return 'Analista';
    }
    if (this.isAdmin()) {
      return 'Administrador';
    }
    if (roles.some(r => {
      const lower = String(r).toLowerCase();
      return lower.includes('vecino') || lower.includes('rveco');
    })) {
      return 'Vecino';
    }
    return roles[0] || null;
  }

  /**
   * Helper paramétrico para determinar si es agente de campo
   */
  isAgenteCampo(): boolean {
    const roles = this.userRoles();
    return roles.some(r => {
      const lower = String(r).toLowerCase();
      return lower.includes('agente') || lower.includes('ragte') || lower === '17';
    });
  }

  /**
   * Helper paramétrico para determinar si es un admin
   */
  isAdmin(): boolean {
    const roles = this.userRoles();
    return roles.some(r => String(r).toLowerCase().includes('admin'));
  }

  /**
   * Helper paramétrico para determinar si es analista
   */
  isAnalista(): boolean {
    const roles = this.userRoles();
    return roles.some(r => {
      const lower = String(r).toLowerCase();
      return lower.includes('analista') || lower.includes('ranta') || lower === '16';
    });
  }

  /**
   * Helper paramétrico para determinar si es usuario interno/staff (admin, analista, agente, operador)
   */
  isInternalUser(): boolean {
    const roles = this.userRoles();
    return roles.some(r => {
      const lower = String(r).toLowerCase();
      return lower.includes('admin') ||
             lower.includes('analista') ||
             lower.includes('ranta') ||
             lower.includes('roper') ||
             lower.includes('agente') ||
             lower.startsWith('sys_');
    });
  }

  private saveSession(token: string, remember: boolean): void {
    if (remember) {
      localStorage.setItem(this.TOKEN_KEY, token);
      localStorage.setItem(this.REMEMBER_KEY, 'true');
      sessionStorage.removeItem(this.TOKEN_KEY);
    } else {
      sessionStorage.setItem(this.TOKEN_KEY, token);
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.REMEMBER_KEY);
    }
  }

  private loadStoredUser(): CurrentUser | null {
    const token = sessionStorage.getItem(this.TOKEN_KEY) || localStorage.getItem(this.TOKEN_KEY);
    if (!token) {
      return null;
    }

    const user = parseCurrentUser(token);
    if (!user || isTokenExpired(Math.floor(user.expiresAt / 1000))) {
      this.clearStorage();
      return null;
    }

    return user;
  }

  private clearStorage(): void {
    sessionStorage.removeItem(this.TOKEN_KEY);
    sessionStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.REMEMBER_KEY);
  }

  /**
   * Obtiene la ruta del dashboard principal según el rol del usuario autenticado
   */
  getDashboardUrl(): string {
    if (this.isAgenteCampo()) {
      return '/agente/dashboard';
    }
    if (this.isAnalista()) {
      return '/analista/dashboard';
    }
    if (this.isAdmin()) {
      return '/admin/dashboard';
    }
    return '/vecino/dashboard';
  }

  /**
   * Redirección inteligente según el rol del usuario autenticado
   */
  redirectToDashboard(roles?: string[]): void {
    if (roles && roles.length > 0) {
      const normalizedRoles = roles.map((r) => String(r).toLowerCase());
      console.log('[AuthService] Evaluando redirección para roles:', roles, 'Normalizados:', normalizedRoles);

      const isAgente = normalizedRoles.some(r =>
        r.includes('agente') ||
        r.includes('ragte') ||
        r === '17'
      );
      const isAnalista = normalizedRoles.some(r =>
        r.includes('analista') ||
        r.includes('ranta') ||
        r === '16'
      );
      const isAdmin = normalizedRoles.some(r =>
        r.includes('admin') ||
        r.includes('radmn')
      );
      const isVecino = normalizedRoles.some(r =>
        r.includes('vecino') ||
        r.includes('rveco')
      );

      if (isAgente) {
        console.log('[AuthService] -> Navegando a /agente/dashboard');
        this.router.navigate(['/agente/dashboard']);
        return;
      } else if (isAnalista) {
        console.log('[AuthService] -> Navegando a /analista/dashboard');
        this.router.navigate(['/analista/dashboard']);
        return;
      } else if (isAdmin) {
        console.log('[AuthService] -> Navegando a /admin/dashboard');
        this.router.navigate(['/admin/dashboard']);
        return;
      } else if (isVecino) {
        console.log('[AuthService] -> Navegando a /vecino/dashboard');
        this.router.navigate(['/vecino/dashboard']);
        return;
      }
    }

    const defaultUrl = this.getDashboardUrl();
    console.log('[AuthService] Navegando a dashboard por defecto:', defaultUrl);
    this.router.navigate([defaultUrl]);
  }
}
