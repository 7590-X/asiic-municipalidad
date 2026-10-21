import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ClarityModule } from '@clr/angular';
import { AuthService } from '../../../../core/services/auth.service';
import { IncidenciasService } from '../../../incidencias/services/incidencias.service';

@Component({
  selector: 'app-dashboard-analista',
  standalone: true,
  imports: [CommonModule, RouterModule, ClarityModule],
  templateUrl: './dashboard-analista.component.html',
  styleUrls: ['./dashboard-analista.component.scss']
})
export class DashboardAnalistaComponent implements OnInit {
  private authService = inject(AuthService);
  private incidenciasService = inject(IncidenciasService);

  loading = signal<boolean>(true);
  incidencias = signal<any[]>([]);

  readonly currentUser = computed(() => this.authService.currentUser());

  // Métricas
  readonly totalAsignadas = computed(() => this.incidencias().length);

  readonly totalAsignadaInicial = computed(() =>
    this.incidencias().filter(i => (i.estado || '').toUpperCase().includes('INCIDENCIA ASIGNADA')).length
  );

  readonly totalApelaciones = computed(() =>
    this.incidencias().filter(i => (i.estado || '').toUpperCase().includes('APELACIÓN') || (i.estado || '').toUpperCase().includes('APELACION')).length
  );

  readonly totalCampo = computed(() =>
    this.incidencias().filter(i => (i.estado || '').toUpperCase().includes('CAMPO')).length
  );

  readonly totalRecoleccion = computed(() =>
    this.incidencias().filter(i => (i.estado || '').toUpperCase().includes('RECOLECCIÓN') || (i.estado || '').toUpperCase().includes('RECOLECCION')).length
  );

  // Desglose por tipo
  readonly totalQuejas = computed(() =>
    this.incidencias().filter(i => (i.tipo || '').toUpperCase().includes('QUEJA')).length
  );

  readonly totalReclamos = computed(() =>
    this.incidencias().filter(i => (i.tipo || '').toUpperCase().includes('RECLAMO')).length
  );

  readonly totalDenuncias = computed(() =>
    this.incidencias().filter(i => (i.tipo || '').toUpperCase().includes('DENUNCIA')).length
  );

  readonly totalSugerencias = computed(() =>
    this.incidencias().filter(i => (i.tipo || '').toUpperCase().includes('SUGERENCIA')).length
  );

  // Top 5 recientes
  readonly incidenciasRecientes = computed(() =>
    this.incidencias().slice(0, 5)
  );

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.loading.set(true);
    this.incidenciasService.getIncidenciasAsignadas().subscribe({
      next: (data) => {
        // En el dashboard de analista NO deben mostrarse incidencias ya ACEPTADAS ni RECHAZADAS,
        // ya que han sido dictaminadas y no requieren acción pendiente del analista.
        const activas = (data || []).filter(i => {
          const st = (i.estado || '').toUpperCase();
          return !st.includes('ACEPTADA') && !st.includes('RECHAZADA');
        });
        const ordenadas = activas.sort((a, b) => Number(a.id) - Number(b.id));
        this.incidencias.set(ordenadas);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando incidencias del analista', err);
        this.loading.set(false);
      }
    });
  }

  getTipoBadgeClass(tipo: string): string {
    const t = (tipo || '').toUpperCase();
    if (t.includes('QUEJA')) return 'label-warning';
    if (t.includes('RECLAMO')) return 'label-warning';
    if (t.includes('DENUNCIA')) return 'label-danger';
    if (t.includes('SUGERENCIA')) return 'label-info';
    return 'label-info';
  }

  getEstadoBadgeClass(estado: string): string {
    const st = (estado || '').toUpperCase();
    if (st.includes('ASIGNADA') && !st.includes('APELACIÓN') && !st.includes('APELACION') && !st.includes('CAMPO')) {
      return 'badge-warning';
    }
    if (st.includes('CAMPO')) {
      return 'badge-blue';
    }
    if (st.includes('APELACIÓN') || st.includes('APELACION')) {
      return 'badge-purple';
    }
    if (st.includes('RECOLECCIÓN') || st.includes('RECOLECCION')) {
      return 'badge-info';
    }
    if (st.includes('ACEPTADA')) return 'badge-success';
    if (st.includes('RECHAZADA')) return 'badge-danger';
    return 'badge-info';
  }
}
