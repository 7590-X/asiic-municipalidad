import { Component, computed, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ClarityModule } from '@clr/angular';
import { AuthService } from '../../../../core/services/auth.service';
import { IncidenciasService } from '../../../incidencias/services/incidencias.service';
import { MetricCardComponent } from '../../../../shared/components/metric-card/metric-card.component';
import { QuickActionCardComponent } from '../../../../shared/components/quick-action-card/quick-action-card.component';
import { ModalPreviewArchivoComponent } from '../../../../shared/components/modal-preview-archivo/modal-preview-archivo.component';

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
  imports: [CommonModule, RouterLink, ClarityModule, MetricCardComponent, QuickActionCardComponent, ModalPreviewArchivoComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private incidenciasService = inject(IncidenciasService);
  private router = inject(Router);

  // Modal de Vista Previa
  previewModalOpen = false;
  archivoEnPreview: any = null;

  abrirPreview(archivo: any): void {
    this.archivoEnPreview = archivo;
    this.previewModalOpen = true;
  }

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

  resumenModalOpen = false;
  resumenSeleccionado: any = null;

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
    if (this.authService.isAnalista() || this.authService.isAgenteCampo() || this.authService.isAdmin()) {
      this.router.navigate([this.authService.getDashboardUrl()]);
      return;
    }

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
      case EIncidenciaEstado.ACEPTADA:
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
      default: {
        if (!estado) return 'badge-info';
        const st = estado.toUpperCase();
        if (st.includes('ACEPTADA') || st.includes('CONFIRMADA') || st.includes('SOLUCIONADA') || st.includes('FINALIZADA') || st.includes('RESUELTO')) return 'badge-success';
        if (st.includes('RECHAZADA') || st.includes('BLOQUEADA') || st.includes('APELADA')) return 'badge-danger';
        if (st.includes('ASIGNADA') || st.includes('SOLUCIONANDO') || st.includes('PROGRESO')) return 'badge-warning';
        return 'badge-info';
      }
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

  abrirResumen(gestion: any): void {
    this.loading.set(true);
    forkJoin({
      incidencia: this.incidenciasService.getIncidenciaById(gestion.id),
      archivos: this.incidenciasService.getArchivosIncidencia(gestion.id)
    }).subscribe({
      next: (data) => {
        const incidencia = data.incidencia;
        const archivos = data.archivos || [];

        if (archivos.length > 0) {
          const peticiones = archivos.map((archivo: any) =>
            this.incidenciasService.descargarArchivoIncidencia(gestion.id, archivo.id).pipe(
              map(blob => {
                const isImage = archivo.formato?.toLowerCase().match(/(jpg|jpeg|png|gif|webp)$/);
                return {
                  ...archivo,
                  blob,
                  url: isImage ? URL.createObjectURL(blob) : null
                };
              }),
              catchError(() => of({ ...archivo, blob: null, url: null }))
            )
          );

          forkJoin(peticiones).subscribe(archivosConUrl => {
            this.resumenSeleccionado = { ...incidencia, archivosAdjuntos: archivosConUrl };
            this.resumenModalOpen = true;
            this.loading.set(false);
          });
        } else {
          this.resumenSeleccionado = { ...incidencia, archivosAdjuntos: [] };
          this.resumenModalOpen = true;
          this.loading.set(false);
        }
      },
      error: (err) => {
        console.error('Error cargando detalles del resumen', err);
        alert('No se pudieron cargar los detalles para el resumen.');
        this.loading.set(false);
      }
    });
  }

  imprimirResumen(): void {
    setTimeout(() => {
      window.print();
    }, 100);
  }

  enviarBorradorDirecto(tramite: any): void {
    if (!tramite?.id) return;
    if (confirm(`¿Deseas enviar formalmente la incidencia #${tramite.id} a revisión municipal?`)) {
      this.loading.set(true);
      this.incidenciasService.enviarIncidencia(tramite.id).subscribe({
        next: () => {
          this.tramites.update(list =>
            list.map(t => t.id === tramite.id ? { ...t, estado: 'Enviada' } : t)
          );
          this.loading.set(false);
          alert(`Incidencia #${tramite.id} enviada exitosamente con estado ENVIADA.`);
        },
        error: (err) => {
          console.error('Error al enviar borrador', err);
          this.loading.set(false);
          alert(`Error al enviar la incidencia #${tramite.id}. Por favor intenta nuevamente.`);
        }
      });
    }
  }
}
