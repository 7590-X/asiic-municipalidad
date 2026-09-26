import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/landing/landing.component').then((m) => m.LandingComponent),
  },
  {
    path: 'registro',
    loadComponent: () =>
      import('./features/vecino/pages/registro-vecino/registro-vecino.component')
        .then((m) => m.RegistroVecinoComponent),
  },
  {
    path: 'confirmar-cuenta',
    loadComponent: () =>
      import('./features/vecino/pages/confirmar-cuenta/confirmar-cuenta.component')
        .then((m) => m.ConfirmarCuentaComponent),
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/pages/login/login.component')
        .then((m) => m.LoginComponent),
  },
  {
    path: 'vecino',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./shared/layouts/portal-layout/portal-layout.component')
        .then((m) => m.PortalLayoutComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/vecino/pages/dashboard/dashboard.component')
            .then((m) => m.DashboardComponent),
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
      {
        path: 'incidencias/nueva',
        loadComponent: () =>
          import('./features/incidencias/pages/registro-incidencia/registro-incidencia.component')
            .then((m) => m.RegistroIncidenciaComponent),
      },
      {
        path: 'incidencias/editar/:id',
        loadComponent: () =>
          import('./features/incidencias/pages/registro-incidencia/registro-incidencia.component')
            .then((m) => m.RegistroIncidenciaComponent),
      }
    ],
  },
  {
    path: 'dashboard',
    redirectTo: 'vecino/dashboard',
    pathMatch: 'full',
  },
  { path: '**', redirectTo: '' },
];

