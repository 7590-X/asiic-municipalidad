import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ClarityModule } from '@clr/angular';
import { AuthService } from '../../../../core/services/auth.service';
import { MetricCardComponent } from '../../components/metric-card/metric-card.component';
import { QuickActionCardComponent } from '../../components/quick-action-card/quick-action-card.component';

export interface TramiteReciente {
  id: string;
  tipo: 'Solicitud' | 'Queja' | 'Denuncia' | 'Sugerencia' | 'Solvencia';
  asunto: string;
  dependencia: string;
  fecha: string;
  estado: 'Pendiente' | 'En Progreso' | 'En Revisión' | 'Resuelto' | 'Finalizado';
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ClarityModule, MetricCardComponent, QuickActionCardComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent {
  private authService = inject(AuthService);

  readonly currentUser = this.authService.currentUser;

  readonly userName = computed(() => {
    return this.currentUser()?.name || 'Vecino';
  });

  readonly currentDate = new Date().toLocaleDateString('es-GT', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  // Métricas para tarjetas de resumen
  readonly metrics = signal({
    solicitudesActivas: 3,
    solicitudesTotal: 8,
    quejasEnProceso: 1,
    notificacionesNuevas: 3,
    estadoSolvencia: 'Solvente',
  });

  // Lista de trámites para el Datagrid de Clarity
  readonly tramites = signal<TramiteReciente[]>([
    {
      id: 'ASIIC-2026-0042',
      tipo: 'Solicitud',
      asunto: 'Reparación de luminaria pública y cableado',
      dependencia: 'Dirección de Alumbrado Público',
      fecha: '15/09/2026',
      estado: 'En Progreso',
    },
    {
      id: 'ASIIC-2026-0038',
      tipo: 'Queja',
      asunto: 'Fuga de agua en banqueta y baja presión residencial',
      dependencia: 'EMPAGUA',
      fecha: '10/09/2026',
      estado: 'Resuelto',
    },
    {
      id: 'ASIIC-2026-0035',
      tipo: 'Solvencia',
      asunto: 'Constancia municipal de solvencia de ornato',
      dependencia: 'Dirección de Administración Financiera',
      fecha: '05/09/2026',
      estado: 'Finalizado',
    },
    {
      id: 'ASIIC-2026-0029',
      tipo: 'Denuncia',
      asunto: 'Obstrucción de paso peatonal por escombros en acera',
      dependencia: 'Policía Municipal de Tránsito (PMT)',
      fecha: '28/08/2026',
      estado: 'En Revisión',
    },
    {
      id: 'ASIIC-2026-0021',
      tipo: 'Sugerencia',
      asunto: 'Propuesta de señalización y reducción de velocidad escolar',
      dependencia: 'Dirección de Movilidad Urbana',
      fecha: '14/08/2026',
      estado: 'Finalizado',
    },
  ]);

  getEstadoBadgeClass(estado: string): string {
    switch (estado) {
      case 'Resuelto':
      case 'Finalizado':
        return 'badge-success';
      case 'En Progreso':
        return 'badge-warning';
      case 'En Revisión':
        return 'badge-info';
      case 'Pendiente':
      default:
        return 'badge-danger';
    }
  }

  getTipoBadgeClass(tipo: string): string {
    switch (tipo) {
      case 'Solicitud':
        return 'label-purple';
      case 'Queja':
        return 'label-orange';
      case 'Denuncia':
        return 'label-danger';
      case 'Solvencia':
        return 'label-success';
      case 'Sugerencia':
      default:
        return 'label-blue';
    }
  }
}
