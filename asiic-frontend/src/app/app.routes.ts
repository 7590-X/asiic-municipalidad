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
      import('./features/registro-vecino/pages/registro-vecino/registro-vecino.component')
        .then((m) => m.RegistroVecinoComponent),
  },
  { path: '**', redirectTo: '' },
];
