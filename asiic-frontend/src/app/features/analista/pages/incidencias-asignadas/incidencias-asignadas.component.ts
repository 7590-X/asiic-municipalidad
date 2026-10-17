import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ClarityModule } from '@clr/angular';
import { IncidenciasService } from '../../../incidencias/services/incidencias.service';
import { ModalPreviewArchivoComponent } from '../../../../shared/components/modal-preview-archivo/modal-preview-archivo.component';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Component({
  selector: 'app-incidencias-asignadas',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ClarityModule, ModalPreviewArchivoComponent],
  templateUrl: './incidencias-asignadas.component.html',
  styleUrls: ['./incidencias-asignadas.component.scss']
})
export class IncidenciasAsignadasComponent implements OnInit {
  private incidenciasService = inject(IncidenciasService);

  // Lista reactiva de incidencias asignadas
  incidencias = signal<any[]>([]);
  loading = signal<boolean>(true);
  procesandoAccion = signal<boolean>(false);

  // Alertas en pantalla (AN1-3 y AN1-4 resultados)
  alertMessage = signal<string | null>(null);
  alertType = signal<'success' | 'danger' | 'info'>('success');

  // Filtros de búsqueda y estado
  filtroBusqueda = signal<string>('');
  filtroTipo = signal<string>('TODOS');
  filtroEstado = signal<string>('TODOS');

  // Modo de visualización: 'cards' para vista móvil touch, 'table' para datagrid
  vistaModo = signal<'cards' | 'table'>('cards');

  // Modales de Detalle y Confirmación
  detalleModalOpen = false;
  selectedIncidencia: any = null;

  // Dialogo de Confirmación Aceptar (AN1-3)
  confirmAceptarOpen = false;

  // Dialogo de Confirmación Rechazar con Feedback para el Vecino (AN1-4 / FA4)
  confirmRechazarOpen = false;
  feedbackTitulo = '';
  feedbackDescripcion = '';
  feedbackRecomendacion = '';
  formRechazoInvalido = false;

  // Modal para Solicitar Más Evidencias con Agente de Campo
  modalCampoOpen = false;
  campoInstrucciones = '';
  campoTipoInspeccion = 'Inspección Ocular y Toma de Fotografías';
  campoPrioridad = 'Normal';
  formCampoInvalido = false;

  // Evidencias cargadas para la incidencia abierta
  evidenciasCargadas: any[] = [];
  cargandoEvidencias = false;

  // Modal de Vista Previa para Imágenes y Documentos PDF
  previewModalOpen = false;
  archivoEnPreview: any = null;

  abrirPreview(archivo: any): void {
    this.archivoEnPreview = archivo;
    this.previewModalOpen = true;
  }

  cerrarPreview(): void {
    this.previewModalOpen = false;
    this.archivoEnPreview = null;
  }

  // Resumen estadístico
  totalAsignadas = computed(() => {
    return this.incidencias().length;
  });

  totalEstadoAsignada = computed(() => {
    return this.incidencias().filter(i => {
      const st = (i.estado || '').toUpperCase();
      return st.includes('ASIGNADA') && !st.includes('APELAC') && !st.includes('CAMPO');
    }).length;
  });

  totalEstadoCampo = computed(() => {
    return this.incidencias().filter(i => (i.estado || '').toUpperCase().includes('CAMPO')).length;
  });

  totalEstadoApelacion = computed(() => {
    return this.incidencias().filter(i => (i.estado || '').toUpperCase().includes('APELAC')).length;
  });

  totalEstadoRecoleccion = computed(() => {
    return this.incidencias().filter(i => (i.estado || '').toUpperCase().includes('RECOLEC')).length;
  });

  // Lista filtrada para la tabla
  incidenciasFiltradas = computed(() => {
    let list = this.incidencias();

    const q = this.filtroBusqueda().trim().toLowerCase();
    if (q) {
      list = list.filter(i =>
        String(i.id).includes(q) ||
        (i.tipo || '').toLowerCase().includes(q) ||
        (i.asunto || '').toLowerCase().includes(q) ||
        (i.nombreVecino || '').toLowerCase().includes(q) ||
        (i.contador || '').toLowerCase().includes(q)
      );
    }

    const tipo = this.filtroTipo().toUpperCase();
    if (tipo !== 'TODOS') {
      list = list.filter(i => (i.tipo || '').toUpperCase().includes(tipo));
    }

    const estado = this.filtroEstado().toUpperCase();
    if (estado !== 'TODOS') {
      list = list.filter(i => (i.estado || '').toUpperCase().includes(estado));
    }

    return list;
  });

  ngOnInit(): void {
    if (typeof window !== 'undefined' && window.innerWidth >= 992) {
      this.vistaModo.set('table');
    } else {
      this.vistaModo.set('cards');
    }
    this.cargarIncidencias();
  }


  cargarIncidencias(): void {
    this.loading.set(true);
    this.incidenciasService.getIncidenciasAsignadas().subscribe({
      next: (data) => {
        // Incluir: Incidencia Asignada, Asignada a Campo, Apelación Asignada, Recolección Finalizada
        const permitidos = ['ASIGNADA', 'CAMPO', 'APELACIÓN', 'APELACION', 'RECOLECCIÓN', 'RECOLECCION'];
        const filtradas = (data || []).filter(item => {
          const st = (item.estado || '').toUpperCase();
          return permitidos.some(p => st.includes(p)) && !st.includes('ACEPTADA') && !st.includes('RECHAZADA');
        });
        this.incidencias.set(filtradas);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error al cargar incidencias asignadas', err);
        this.loading.set(false);
        this.mostrarAlerta('No se pudieron obtener las incidencias asignadas desde el servidor.', 'danger');
      }
    });
  }

  /**
   * Abre el diálogo con la información de la incidencia según su tipo:
   * Queja, Reclamo, Denuncia o Sugerencia
   */
  abrirDetalle(incidencia: any): void {
    this.selectedIncidencia = { ...incidencia };
    this.detalleModalOpen = true;
    this.evidenciasCargadas = [];

    // Cargar evidencias si existen
    if (incidencia.archivosAdjuntos && incidencia.archivosAdjuntos.length > 0) {
      this.evidenciasCargadas = [...incidencia.archivosAdjuntos];
    } else {
      this.cargarEvidencias(incidencia.id);
    }
  }

  cerrarDetalle(): void {
    this.detalleModalOpen = false;
    this.selectedIncidencia = null;
    this.evidenciasCargadas = [];
    this.cerrarPreview();
  }

  cargarEvidencias(incidenciaId: number): void {
    this.cargandoEvidencias = true;
    this.incidenciasService.getArchivosIncidenciaAnalista(incidenciaId).subscribe({
      next: (archivos) => {
        if (!archivos || archivos.length === 0) {
          this.evidenciasCargadas = [];
          this.cargandoEvidencias = false;
          return;
        }

        const peticiones = archivos.map((archivo: any) =>
          this.incidenciasService.descargarArchivoIncidencia(incidenciaId, archivo.id).pipe(
            map(blob => {
              if (blob && blob.size > 0) {
                const isImage = (archivo.formato || archivo.nombre || '').toLowerCase().match(/(jpg|jpeg|png|gif|webp)$/);
                return {
                  ...archivo,
                  blob,
                  url: isImage ? URL.createObjectURL(blob) : null
                };
              }
              return { ...archivo, blob: null, url: null };
            }),
            catchError(() => of({ ...archivo, blob: null, url: null }))
          )
        );

        forkJoin(peticiones).subscribe({
          next: (archivosConUrl) => {
            this.evidenciasCargadas = archivosConUrl;
            this.cargandoEvidencias = false;
          },
          error: () => {
            this.evidenciasCargadas = archivos.map(a => ({ ...a, url: null }));
            this.cargandoEvidencias = false;
          }
        });
      },
      error: () => {
        this.evidenciasCargadas = [];
        this.cargandoEvidencias = false;
      }
    });
  }

  descargarEvidencia(archivo: any): void {
    if (archivo.blob && archivo.blob.size > 0) {
      const a = document.createElement('a');
      const objectUrl = URL.createObjectURL(archivo.blob);
      a.href = objectUrl;
      a.download = archivo.nombre || 'evidencia_incidencia';
      a.click();
      URL.revokeObjectURL(objectUrl);
    } else if (archivo.url) {
      const a = document.createElement('a');
      a.href = archivo.url;
      a.target = '_blank';
      a.download = archivo.nombre || 'evidencia_incidencia';
      a.click();
    } else if (this.selectedIncidencia?.id && archivo.id) {
      this.incidenciasService.descargarArchivoIncidencia(this.selectedIncidencia.id, archivo.id).subscribe({
        next: (blob) => {
          if (blob && blob.size > 0) {
            const a = document.createElement('a');
            const objectUrl = URL.createObjectURL(blob);
            a.href = objectUrl;
            a.download = archivo.nombre || 'evidencia_incidencia';
            a.click();
            URL.revokeObjectURL(objectUrl);
          } else {
            this.mostrarAlerta(`El archivo "${archivo.nombre}" está registrado en la incidencia.`, 'info');
          }
        },
        error: () => {
          this.mostrarAlerta(`El archivo "${archivo.nombre}" está asociado en la base de datos de evidencias.`, 'info');
        }
      });
    }
  }

  // ==========================================
  // FLUJO NORMAL BÁSICO: Aceptar Incidencia (AN1-3)
  // ==========================================

  /**
   * El usuario presiona el botón "Aceptar Incidencia" (paso 2.3.7)
   * Despliega diálogo de confirmación AN1-3
   */
  solicitarAceptarIncidencia(): void {
    this.confirmAceptarOpen = true;
  }

  /**
   * El usuario presiona "Si" en el diálogo AN1-3 (paso 2.3.9)
   * 1. Cierra el diálogo de confirmación
   * 2. Cambia el estado a "Incidencia Aceptada"
   * 3. Cierra el diálogo de detalle
   * 4. Elimina la solicitud de la tabla
   */
  ejecutarAceptar(): void {
    if (!this.selectedIncidencia) return;

    const id = this.selectedIncidencia.id;
    this.procesandoAccion.set(true);

    this.incidenciasService.aceptarIncidencia(id).subscribe({
      next: () => {
        // 1. Cierra el diálogo de confirmación
        this.confirmAceptarOpen = false;

        // 2. Cierra el diálogo con la información de la incidencia
        this.detalleModalOpen = false;
        this.selectedIncidencia = null;

        // 3. Elimina la solicitud de la tabla
        this.incidencias.update(list => list.filter(item => item.id !== id));

        this.procesandoAccion.set(false);

        // 4. Muestra mensaje en pantalla
        this.mostrarAlerta(`Incidencia #${id} aceptada con éxito. El estado cambió a "Incidencia Aceptada".`, 'success');
      },
      error: (err) => {
        console.error('Error al aceptar incidencia', err);
        this.procesandoAccion.set(false);
        this.confirmAceptarOpen = false;
        this.mostrarAlerta(`Error al aceptar la incidencia #${id}. Por favor intente nuevamente.`, 'danger');
      }
    });
  }

  // ==========================================
  // FLUJO ALTERNO FA4: Rechazo de Incidencia con Feedback (AN1-4)
  // ==========================================

  /**
   * El usuario presiona el botón "Rechazar Incidencia" (FA4)
   * Despliega diálogo de confirmación con formulario de retroalimentación para el vecino
   */
  solicitarRechazarIncidencia(incidencia?: any): void {
    if (incidencia) {
      this.selectedIncidencia = { ...incidencia };
    }
    this.feedbackTitulo = '';
    this.feedbackDescripcion = '';
    this.feedbackRecomendacion = '';
    this.formRechazoInvalido = false;
    this.confirmRechazarOpen = true;
  }

  /**
   * El analista confirma el rechazo de la incidencia:
   * 1. Valida el feedback obligatorio (título y descripción)
   * 2. Envía List<IncidenciaFeedback> al endpoint POST /{id}/event/rechazar-incidencia
   * 3. Remueve la incidencia de la tabla y notifica en pantalla
   */
  ejecutarRechazar(): void {
    if (!this.selectedIncidencia) return;

    if (!this.feedbackTitulo.trim() || !this.feedbackDescripcion.trim()) {
      this.formRechazoInvalido = true;
      return;
    }

    const id = this.selectedIncidencia.id;
    this.procesandoAccion.set(true);

    const feedbackPayload = [
      {
        titulo: this.feedbackTitulo.trim(),
        descripcion: this.feedbackDescripcion.trim(),
        recomendacion: this.feedbackRecomendacion.trim() || undefined
      }
    ];

    this.incidenciasService.rechazarIncidencia(id, feedbackPayload).subscribe({
      next: () => {
        // Cierra los diálogos
        this.confirmRechazarOpen = false;
        this.detalleModalOpen = false;
        this.selectedIncidencia = null;

        // Elimina de la tabla la solicitud rechazada
        this.incidencias.update(list => list.filter(item => item.id !== id));
        this.procesandoAccion.set(false);

        // Muestra alerta indicando que la retroalimentación fue generada para el vecino
        this.mostrarAlerta(
          `Incidencia #${id} rechazada. La retroalimentación fue registrada para el vecino y el caso cambió a "Incidencia Rechazada".`,
          'danger'
        );
      },
      error: (err) => {
        console.error('Error al rechazar incidencia', err);
        this.procesandoAccion.set(false);
        this.confirmRechazarOpen = false;
        this.mostrarAlerta(`Error al rechazar la incidencia #${id}. Por favor intente nuevamente.`, 'danger');
      }
    });
  }

  // ==========================================
  // FLUJO: Solicitar Más Evidencias con Agente de Campo
  // ==========================================

  /**
   * Abre el modal para que el analista solicite inspección/evidencias en sitio a un Agente de Campo
   */
  abrirModalCampo(incidencia?: any): void {
    if (incidencia) {
      this.selectedIncidencia = { ...incidencia };
    }
    this.campoInstrucciones = '';
    this.campoTipoInspeccion = 'Inspección Ocular y Toma de Fotografías';
    this.campoPrioridad = 'Normal';
    this.formCampoInvalido = false;
    this.modalCampoOpen = true;
  }

  cerrarModalCampo(): void {
    this.modalCampoOpen = false;
    this.formCampoInvalido = false;
  }

  /**
   * Ejecuta la orden de asignación de campo para recolectar evidencias
   */
  ejecutarSolicitudCampo(): void {
    if (!this.selectedIncidencia) return;

    if (!this.campoInstrucciones.trim()) {
      this.formCampoInvalido = true;
      return;
    }

    const id = this.selectedIncidencia.id;
    this.procesandoAccion.set(true);

    const datosCampo = {
      instrucciones: this.campoInstrucciones.trim(),
      tipoInspeccion: this.campoTipoInspeccion,
      prioridad: this.campoPrioridad
    };

    this.incidenciasService.solicitarEvidenciasCampo(id, datosCampo).subscribe({
      next: () => {
        this.modalCampoOpen = false;
        const fechaNow = new Date().toISOString();

        if (this.selectedIncidencia) {
          this.selectedIncidencia.estado = 'Asignada a Campo';
          this.selectedIncidencia.solicitudCampo = { ...datosCampo, fechaSolicitud: fechaNow };
        }

        // Actualiza el estado reactivamente en la tabla
        this.incidencias.update(list =>
          list.map(item =>
            item.id === id
              ? { ...item, estado: 'Asignada a Campo', solicitudCampo: { ...datosCampo, fechaSolicitud: fechaNow } }
              : item
          )
        );

        this.procesandoAccion.set(false);
        this.mostrarAlerta(
          `Solicitud de recolección enviada al Agente de Campo. La incidencia #${id} cambió a "Asignada a Campo".`,
          'info'
        );
      },
      error: (err) => {
        console.error('Error al solicitar evidencias de campo', err);
        this.procesandoAccion.set(false);
        this.modalCampoOpen = false;
        this.mostrarAlerta(`Error al solicitar evidencias para la incidencia #${id}.`, 'danger');
      }
    });
  }

  /**
   * Permite registrar o simular la finalización de recolección de campo
   */
  ejecutarFinalizarRecoleccion(incidencia?: any): void {
    const target = incidencia || this.selectedIncidencia;
    if (!target) return;
    const id = target.id;
    this.procesandoAccion.set(true);

    this.incidenciasService.finalizarRecoleccionCampo(id).subscribe({
      next: () => {
        if (this.selectedIncidencia && this.selectedIncidencia.id === id) {
          this.selectedIncidencia.estado = 'Recolección Finalizada';
        }
        this.incidencias.update(list =>
          list.map(item => item.id === id ? { ...item, estado: 'Recolección Finalizada' } : item)
        );
        this.procesandoAccion.set(false);
        this.mostrarAlerta(
          `Recolección de evidencias finalizada para la incidencia #${id}. El caso está listo para tu dictamen definitivo.`,
          'success'
        );
      },
      error: (err) => {
        console.error('Error al finalizar recolección', err);
        this.procesandoAccion.set(false);
        this.mostrarAlerta(`Error al registrar finalización de recolección para la incidencia #${id}.`, 'danger');
      }
    });
  }

  // ==========================================
  // FLUJO ALTERNO FA5: Cancelar Confirmación Diálogo
  // ==========================================

  cancelarConfirmacion(): void {
    this.confirmAceptarOpen = false;
    this.confirmRechazarOpen = false;
    this.modalCampoOpen = false;
  }

  // ==========================================
  // HELPERS DE TIPO Y ESTILO
  // ==========================================

  isTipoQueja(tipo?: string): boolean {
    return (tipo || '').toUpperCase().includes('QUEJA');
  }

  isTipoReclamo(tipo?: string): boolean {
    return (tipo || '').toUpperCase().includes('RECLAMO');
  }

  isTipoDenuncia(tipo?: string): boolean {
    return (tipo || '').toUpperCase().includes('DENUNCIA');
  }

  isTipoSugerencia(tipo?: string): boolean {
    return (tipo || '').toUpperCase().includes('SUGERENCIA');
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
    if (st.includes('CAMPO')) {
      return 'badge-blue';
    }
    if (st.includes('ASIGNADA') && !st.includes('APELACIÓN') && !st.includes('APELACION')) {
      return 'badge-warning';
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

  private mostrarAlerta(msg: string, tipo: 'success' | 'danger' | 'info'): void {
    this.alertMessage.set(msg);
    this.alertType.set(tipo);
    setTimeout(() => {
      this.alertMessage.set(null);
    }, 7000);
  }
}
