import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const notification = inject(NotificationService);

  const token = authService.getToken();

  let authReq = req;

  // Agregar el token a los headers si existe
  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Ignorar errores de autenticación/autorización en rutas públicas o de login
      const isPublicRoute = req.url.includes('/auth/') || req.url.includes('/public/');

      if (error.status === 401) {
        if (!isPublicRoute) {
          // No autorizado o token expirado
          authService.logout();
          notification.error('Sesión expirada. Por favor, inicie sesión nuevamente.');
        }
      } else if (error.status === 403) {
        if (!isPublicRoute) {
          // Prohibido (no tiene permisos)
          notification.error('Acceso denegado: No tiene permisos para realizar esta acción.');
        }
      }
      return throwError(() => error);
    })
  );
};
