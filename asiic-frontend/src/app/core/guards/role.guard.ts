import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Evita que usuarios con rol de analista, agente o admin vean el dashboard del vecino.
 * Si entran a /vecino/dashboard son redirigidos de inmediato a su propio dashboard.
 */
export const vecinoDashboardGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isAnalista()) {
    return router.parseUrl('/analista/dashboard');
  }
  if (auth.isAgenteCampo()) {
    return router.parseUrl('/agente/dashboard');
  }
  if (auth.isAdmin()) {
    return router.parseUrl('/admin/dashboard');
  }

  return true;
};

/**
 * Protege las rutas de analista
 */
export const analistaGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return router.parseUrl('/login');
  }
  if (auth.isAnalista() || auth.isAdmin()) {
    return true;
  }
  return router.parseUrl(auth.getDashboardUrl());
};

/**
 * Protege las rutas del agente de campo
 */
export const agenteGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return router.parseUrl('/login');
  }
  if (auth.isAgenteCampo() || auth.isAdmin()) {
    return true;
  }
  return router.parseUrl(auth.getDashboardUrl());
};

/**
 * Protege las rutas de administración general
 */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return router.parseUrl('/login');
  }
  if (auth.isAdmin()) {
    return true;
  }
  return router.parseUrl(auth.getDashboardUrl());
};
