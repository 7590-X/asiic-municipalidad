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
      tap((response) => {
        if (response?.payload?.access_token) {
          const token = response.payload.access_token;
          const user = parseCurrentUser(token);

          if (user) {
            this.saveSession(token, credentials.recordar ?? false);
            this.currentUser.set(user);
            this.redirectToDashboard(user.roles);
          }
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
    if (roles.includes('Administrador') || roles.includes('ADMINISTRADOR')) {
      return 'Administrador';
    }
    if (roles.includes('Vecino') || roles.includes('VECINO')) {
      return 'Vecino';
    }
    return roles[0] || null;
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

  private redirectToDashboard(roles: string[]): void {
    // Manejo de roles para redirección
    const normalizedRoles = roles.map((r) => r.toLowerCase());

    if (normalizedRoles.some(r => r.includes('vecino'))) {
      this.router.navigate(['/vecino/dashboard']).catch(() => this.router.navigate(['/']));
    } else if (normalizedRoles.some(r => r.includes('admin'))) {
      this.router.navigate(['/admin/dashboard']).catch(() => this.router.navigate(['/']));
    } else {
      this.router.navigate(['/']);
    }
  }
}
