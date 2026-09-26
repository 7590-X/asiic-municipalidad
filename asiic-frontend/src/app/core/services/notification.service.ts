import { Injectable, signal } from '@angular/core';

export type AlertType = 'success' | 'warning' | 'danger' | 'info';

export interface Alert {
  id: string;
  type: AlertType;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  alerts = signal<Alert[]>([]);

  show(message: string, type: AlertType = 'info', timeout: number = 5000) {
    const id = Math.random().toString(36).substring(2, 9);
    const newAlert: Alert = { id, type, message };
    
    this.alerts.update(alerts => [...alerts, newAlert]);

    if (timeout > 0) {
      setTimeout(() => this.remove(id), timeout);
    }
  }

  success(message: string, timeout?: number) { this.show(message, 'success', timeout); }
  error(message: string, timeout?: number) { this.show(message, 'danger', timeout); }
  warning(message: string, timeout?: number) { this.show(message, 'warning', timeout); }
  info(message: string, timeout?: number) { this.show(message, 'info', timeout); }

  remove(id: string) {
    this.alerts.update(alerts => alerts.filter(a => a.id !== id));
  }
}
