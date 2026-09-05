import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ClarityModule } from '@clr/angular';
import { StepCardComponent, StepCardData } from '../../shared/components/step-card/step-card.component';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink, ClarityModule, StepCardComponent],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss',
})
export class LandingComponent {
  steps: StepCardData[] = [
    {
      badge: 1,
      title: 'Reporte incidencias',
      description: 'Quejas, reclamos, denuncias y sugerencias en minutos.',
    },
    {
      badge: 2,
      title: 'Dé seguimiento',
      description: 'Consulte el estado de sus gestiones en todo momento.',
    },
    {
      badge: 3,
      title: 'Reciba notificaciones',
      description: 'Le avisamos por correo cada avance importante.',
    },
  ];
}

