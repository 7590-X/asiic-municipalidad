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
      if (error.status === 401) {
        // No autorizado o token expirado
        authService.logout();
        notification.error('Sesión expirada. Por favor, inicie sesión nuevamente.');
      } else if (error.status === 403) {
        // Prohibido (no tiene permisos)
        notification.error('Acceso denegado: No tiene permisos para realizar esta acción.');
        // Opcional: Redirigir a una página de acceso denegado
        // router.navigate(['/acceso-denegado']);
      }
      return throwError(() => error);
    })
  );
};
