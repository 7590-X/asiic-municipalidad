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
        .then((m) => m.RegistroVecinoComponent),
  },
  { path: '**', redirectTo: '' },
];
