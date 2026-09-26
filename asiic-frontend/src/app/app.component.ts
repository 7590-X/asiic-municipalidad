import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ClarityModule } from '@clr/angular';
import { AlertsComponent } from './shared/components/alerts/alerts.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ClarityModule, AlertsComponent],
  template: `
    <app-alerts></app-alerts>
    <router-outlet />
  `,
})
export class AppComponent {}
