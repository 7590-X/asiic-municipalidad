import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const notification = inject(NotificationService);

  // Rutas públicas que no deben llevar el header Authorization
  const isAuthOrPublicEndpoint =
    req.url.includes('/auth/login') ||
    req.url.includes('/auth/confirmar') ||
    req.url.includes('/public/');

  // Validar si la petición es a nuestra API interna
  const isInternalApi =
    req.url.startsWith(environment.apiBaseUrl) ||
    req.url.startsWith('/api/') ||
    !req.url.startsWith('http');

  let authReq = req;
  const token = authService.getToken();

  // Adjuntar Bearer token únicamente a peticiones internas no exentas
  if (token && isInternalApi && !isAuthOrPublicEndpoint && !req.headers.has('Authorization')) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // En peticiones protegidas (no login), un 401 significa expiración o token inválido
        if (!isAuthOrPublicEndpoint) {
          authService.logout();
          notification.error('Su sesión ha expirado o no está autorizado. Inicie sesión nuevamente.');
        }
      } else if (error.status === 403) {
        if (!isAuthOrPublicEndpoint) {
          notification.error('Acceso denegado: No cuenta con permisos para realizar esta acción.');
        }
      }

      return throwError(() => error);
    })
  );
};
