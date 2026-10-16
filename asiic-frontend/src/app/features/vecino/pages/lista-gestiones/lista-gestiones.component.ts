import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
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

  resumenModalOpen = false;
  resumenSeleccionado: any = null;

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

  getEtapaLabel(etapa: any, estado: string): string {
    if (etapa.id === 1 && estado && estado.toUpperCase().includes('BORRADOR')) {
      return 'Borrador';
    }
    return etapa.label;
  }

  getEtapaIcon(etapa: any, estado: string): string {
    if (etapa.id === 1 && estado && estado.toUpperCase().includes('BORRADOR')) {
      return 'pencil';
    }
    return etapa.icon;
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
                  url: isImage ? URL.createObjectURL(blob) : null
                };
              }),
              catchError(() => of({ ...archivo, url: null }))
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
    // La impresión se controla por CSS (@media print) en los estilos.
    setTimeout(() => {
      window.print();
    }, 100);
  }
}
