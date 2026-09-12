import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LoginRequest {
  correo: string;
  password?: string;
}

export interface LoginResponse {
  token: string; // El token JWT
  role: string;  // El rol del usuario (Vecino, Administrador, etc.)
  // Agrega otras propiedades si el backend retorna más datos (ej. id, nombre, etc)
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly TOKEN_KEY = 'auth_token';
  private readonly ROLE_KEY = 'user_role';

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, credentials).pipe(
      tap(response => {
        if (response && response.token) {
          this.setToken(response.token);
          this.setRole(response.role);
          this.redirectToDashboard(response.role);
        }
      })
    );
  }

  logout(): void {
    this.removeToken();
    this.removeRole();
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return sessionStorage.getItem(this.TOKEN_KEY);
  }

  setToken(token: string): void {
    sessionStorage.setItem(this.TOKEN_KEY, token);
  }

  removeToken(): void {
    sessionStorage.removeItem(this.TOKEN_KEY);
  }

  getRole(): string | null {
    return sessionStorage.getItem(this.ROLE_KEY);
  }

  setRole(role: string): void {
    sessionStorage.setItem(this.ROLE_KEY, role);
  }

  removeRole(): void {
    sessionStorage.removeItem(this.ROLE_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private redirectToDashboard(role: string): void {
    // Definimos el redireccionamiento dependiendo del rol
    if (role === 'Vecino') {
      this.router.navigate(['/vecino/dashboard']);
    } else if (role === 'Administrador') {
      this.router.navigate(['/admin/dashboard']);
    } else {
      this.router.navigate(['/']); // Ruta por defecto si no hay un dashboard especifico
    }
  }
}
