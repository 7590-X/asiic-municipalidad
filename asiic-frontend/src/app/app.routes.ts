import { Routes } from '@angular/router';

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
  // Ruta protegida con authGuard:
  // {
  //   path: 'vecino/dashboard',
  //   canActivate: [authGuard],
  //   loadComponent: () => import('...').then((m) => m.DashboardComponent),
  // },
  { path: '**', redirectTo: '' },
];
