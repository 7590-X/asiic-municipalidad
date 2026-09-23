import { Component, computed, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ClarityModule } from '@clr/angular';
import { AuthService } from '../../../../core/services/auth.service';
import { IncidenciasService } from '../../../incidencias/services/incidencias.service';
import { MetricCardComponent } from '../../../../shared/components/metric-card/metric-card.component';
import { QuickActionCardComponent } from '../../../../shared/components/quick-action-card/quick-action-card.component';

export enum EIncidenciaEstado {
  BORRADOR = 'Borrador',
  ENVIADA = 'Enviada',
  ASIGNADA = 'Incidencia Asignada',
  ACEPTADA = 'Incidencia Aceptada',
  ASIGNADA_CAMPO = 'Asignada Campo',
  RECHAZADA = 'Incidencia Rechazada',
  APELADA = 'Incidencia Apelada',
  APELACION_ASIGNADA = 'Apelación Asignada',
  RECOLECCION_FIN = 'Recolección Finalizada',
  CONFIRMADA = 'Incidencia Confirmada',
  FINALIZADA = 'Incidencia Finalizada',
  APERTURADA = 'Incidencia Aperturada',
  SOLUCIONANDO = 'Solucionando Incidencia',
  SOLUCIONADA = 'Incidencia Solucionada',
  BLOQUEADA = 'Incidencia Bloqueada'
}

export interface TramiteReciente {
  id: string;
  tipo: string;
  asunto: string;
  dependencia: string;
  fecha: string;
  estado: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, ClarityModule, MetricCardComponent, QuickActionCardComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private incidenciasService = inject(IncidenciasService);

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

  // Lista de trámites dinámica
  readonly tramites = signal<TramiteReciente[]>([]);
  readonly loading = signal<boolean>(true);

  // Métricas para tarjetas de resumen
  readonly metrics = computed(() => {
    const todos = this.tramites();
    const activas = todos.filter(t => 
      t.estado !== EIncidenciaEstado.FINALIZADA && 
      t.estado !== EIncidenciaEstado.SOLUCIONADA && 
      t.estado !== EIncidenciaEstado.RECHAZADA
    ).length;
    
    const quejasEnProceso = todos.filter(t => 
      (t.tipo === 'Queja' || t.tipo === 'Reclamo' || t.tipo === 'QUEJA' || t.tipo === 'RECLAMO') && 
      t.estado !== EIncidenciaEstado.BORRADOR && 
      t.estado !== EIncidenciaEstado.FINALIZADA && 
      t.estado !== EIncidenciaEstado.SOLUCIONADA &&
      t.estado !== EIncidenciaEstado.RECHAZADA
    ).length;
    
    return {
      solicitudesActivas: activas,
      solicitudesTotal: todos.length,
      quejasEnProceso: quejasEnProceso,
      notificacionesNuevas: 0,
      estadoSolvencia: 'Solvente',
    };
  });

  ngOnInit() {
    this.incidenciasService.getMisIncidencias().subscribe({
      next: (data) => {
        this.tramites.set(data as TramiteReciente[]);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error al cargar incidencias', err);
        this.loading.set(false);
      }
    });
  }

  getEstadoBadgeClass(estado: string): string {
    switch (estado) {
      case EIncidenciaEstado.CONFIRMADA:
      case EIncidenciaEstado.SOLUCIONADA:
      case EIncidenciaEstado.FINALIZADA:
        return 'badge-success';
      case EIncidenciaEstado.ASIGNADA:
      case EIncidenciaEstado.ASIGNADA_CAMPO:
      case EIncidenciaEstado.APELACION_ASIGNADA:
      case EIncidenciaEstado.SOLUCIONANDO:
        return 'badge-warning';
      case EIncidenciaEstado.ENVIADA:
      case EIncidenciaEstado.BORRADOR:
      case EIncidenciaEstado.RECOLECCION_FIN:
      case EIncidenciaEstado.APERTURADA:
        return 'badge-info';
      case EIncidenciaEstado.RECHAZADA:
      case EIncidenciaEstado.BLOQUEADA:
      case EIncidenciaEstado.APELADA:
        return 'badge-danger';
      default:
        return 'badge-info';
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
