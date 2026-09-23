import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { ClarityModule } from '@clr/angular';
import { IncidenciasService } from '../../../incidencias/services/incidencias.service';

@Component({
  selector: 'app-lista-gestiones',
  standalone: true,
  imports: [CommonModule, RouterModule, ClarityModule],
  templateUrl: './lista-gestiones.component.html',
  styleUrls: ['./lista-gestiones.component.scss']
})
export class ListaGestionesComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private incidenciasService = inject(IncidenciasService);

  gestiones = signal<any[]>([]);
  loading = signal<boolean>(true);
  
  titulo = signal<string>('Mis Solicitudes');
  descripcion = signal<string>('Historial completo de gestiones');
  icono = signal<string>('folder');

  ngOnInit() {
    this.route.data.subscribe(data => {
      const allowedTypes = data['tipos'] as string[] | undefined;
      this.configurarCabecera(allowedTypes);
      this.cargarGestiones(allowedTypes);
    });
  }

  configurarCabecera(tipos?: string[]) {
    if (!tipos || tipos.length === 0) {
      this.titulo.set('Mis Solicitudes');
      this.descripcion.set('Historial de todas tus gestiones y reportes');
      this.icono.set('folder');
    } else if (tipos.includes('Queja') || tipos.includes('Reclamo') || tipos.includes('QUEJA') || tipos.includes('RECLAMO')) {
      this.titulo.set('Quejas y Reclamos');
      this.descripcion.set('Seguimiento a tus reportes de averías y deficiencias');
      this.icono.set('exclamation-circle');
    } else if (tipos.includes('Denuncia') || tipos.includes('DENUNCIA')) {
      this.titulo.set('Denuncias Ciudadanas');
      this.descripcion.set('Seguimiento a denuncias de infracciones');
      this.icono.set('shield');
    } else if (tipos.includes('Sugerencia') || tipos.includes('SUGERENCIA')) {
      this.titulo.set('Buzón de Sugerencias');
      this.descripcion.set('Estado de tus propuestas de mejora');
      this.icono.set('talk-bubbles');
    }
  }

  cargarGestiones(allowedTypes?: string[]) {
    this.loading.set(true);
    this.incidenciasService.getMisIncidencias().subscribe({
      next: (data) => {
        if (!allowedTypes || allowedTypes.length === 0) {
          this.gestiones.set(data);
        } else {
          // Normalizar el tipo para el filtro
          const allowedUpper = allowedTypes.map(t => t.toUpperCase());
          const filtered = data.filter(g => allowedUpper.includes(g.tipo?.toUpperCase()));
          this.gestiones.set(filtered);
        }
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando gestiones', err);
        this.loading.set(false);
      }
    });
  }

  getTipoBadgeClass(tipo: string): string {
    switch(tipo?.toUpperCase()) {
      case 'QUEJA': return 'label-warning';
      case 'RECLAMO': return 'label-warning';
      case 'DENUNCIA': return 'label-danger';
      case 'SUGERENCIA': return 'label-info';
      default: return 'label-info';
    }
  }

  getEstadoBadgeClass(estado: string): string {
    if (!estado) return 'badge-info';
    const st = estado.toUpperCase();
    if (st.includes('BORRADOR')) return 'badge-info';
    if (st.includes('ENVIADA')) return 'badge-primary';
    if (st.includes('PROCESO') || st.includes('ASIGNADA') || st.includes('ACEPTADA') || st.includes('APERTURADA') || st.includes('SOLUCIONANDO')) return 'badge-warning';
    if (st.includes('RECHAZADA') || st.includes('BLOQUEADA')) return 'badge-danger';
    if (st.includes('FINALIZADA') || st.includes('SOLUCIONADA') || st.includes('CONFIRMADA')) return 'badge-success';
    return 'badge-info';
  }

  etapas = [
    { id: 1, label: 'Registrada', icon: 'clipboard' },
    { id: 2, label: 'En Revisión', icon: 'search' },
    { id: 3, label: 'En Proceso', icon: 'cog' },
    { id: 4, label: 'Finalizada', icon: 'check-circle' }
  ];

  getEtapaActual(estado: string): number {
    if (!estado) return 1;
    const st = estado.toUpperCase();
    if (st.includes('BORRADOR') || st.includes('ENVIADA')) return 1;
    if (st.includes('ACEPTADA') || st.includes('ASIGNADA') || st.includes('APERTURADA') || st.includes('APELADA')) return 2;
    if (st.includes('SOLUCIONANDO') || st.includes('CAMPO') || st.includes('RECOLECCION')) return 3;
    if (st.includes('FINALIZADA') || st.includes('SOLUCIONADA') || st.includes('CONFIRMADA') || st.includes('RECHAZADA') || st.includes('BLOQUEADA')) return 4;
    return 1;
  }
}
