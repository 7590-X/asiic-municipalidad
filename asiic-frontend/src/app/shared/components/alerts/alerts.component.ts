import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [CommonModule, ClarityModule],
  template: `
    <div class="floating-alerts-container">
      @for (alert of notificationService.alerts(); track alert.id) {
        <clr-alert [clrAlertType]="alert.type" [clrAlertClosable]="true" (clrAlertClosedChange)="onClose(alert.id)">
          <clr-alert-item>
            <span class="alert-text">{{ alert.message }}</span>
          </clr-alert-item>
        </clr-alert>
      }
    </div>
  `,
  styles: [`
    .floating-alerts-container {
      position: fixed;
      top: 1rem;
      right: 1rem;
      z-index: 1060; /* Asegurar que esté por encima de modales o layouts (Clarity usa 1050 para modales) */
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      max-width: 400px;
      width: 100%;
    }
  `]
})
export class AlertsComponent {
  notificationService = inject(NotificationService);

  onClose(id: string) {
    this.notificationService.remove(id);
  }
}
