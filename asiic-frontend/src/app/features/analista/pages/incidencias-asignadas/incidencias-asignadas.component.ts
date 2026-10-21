import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ClarityModule } from '@clr/angular';
import { IncidenciasService, IncidenciaFeedback } from '../../../incidencias/services/incidencias.service';
import { ModalPreviewArchivoComponent } from '../../../../shared/components/modal-preview-archivo/modal-preview-archivo.component';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface MotivoRechazoItem {
  titulo: string;
  descripcion: string;
  recomendacion?: string;
}

@Component({
  selector: 'app-incidencias-asignadas',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ClarityModule, ModalPreviewArchivoComponent],
  templateUrl: './incidencias-asignadas.component.html',
  styleUrls: ['./incidencias-asignadas.component.scss']
})
export class IncidenciasAsignadasComponent implements OnInit {
  private incidenciasService = inject(IncidenciasService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

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

  // Dialogo de Confirmación Rechazar con Listado de Feedback para el Vecino (AN1-4 / FA4)
  confirmRechazarOpen = false;
  feedbackTitulo = '';
  feedbackDescripcion = '';
  feedbackRecomendacion = '';
  listaMotivosRechazo: MotivoRechazoItem[] = [];
  editandoIndice: number | null = null;
  formRechazoInvalido = false;
  errorListaVacia = false;

  // Catálogo de plantillas rápidas de motivos frecuentes
  plantillasRechazo = [
    {
      label: 'Falta de evidencias',
      icon: 'file',
      titulo: 'Falta de pruebas o evidencias concluyentes',
      descripcion: 'La solicitud no presenta fotografías, documentos o elementos de convicción suficientes que permitan constatar los hechos expuestos.',
      recomendacion: 'Puede ingresar una nueva gestión adjuntando fotografías claras, documentos o recibos que respalden su solicitud.'
    },
    {
      label: 'Fuera de jurisdicción',
      icon: 'folder',
      titulo: 'Improcedente por falta de competencia territorial',
      descripcion: 'La dirección o inmueble indicado se encuentra fuera del área de jurisdicción y cobertura técnica de esta municipalidad.',
      recomendacion: 'Le sugerimos canalizar su solicitud ante la municipalidad u organismo gubernamental correspondiente a su sector.'
    },
    {
      label: 'Datos incompletos',
      icon: 'user',
      titulo: 'Información incompleta o no localizable',
      descripcion: 'Los datos de contacto, número de contador o descripción de ubicación impiden realizar la inspección técnica correspondiente.',
      recomendacion: 'Verifique su número de contador y referencias exactas (calle, número y zona) al registrar un nuevo reporte.'
    },
    {
      label: 'Trámite duplicado',
      icon: 'file-group',
      titulo: 'Reporte duplicado previamente registrado',
      descripcion: 'Existe un trámite idéntico registrado previamente en la plataforma para este mismo domicilio y hecho, el cual sigue en curso.',
      recomendacion: 'Puede dar seguimiento al caso ya abierto desde su portal de vecino o aportar información complementaria sobre el mismo.'
    },
    {
      label: 'No competencia municipal',
      icon: 'shield',
      titulo: 'Materia ajena a los servicios municipales',
      descripcion: 'El asunto planteado corresponde a controversias privadas entre particulares o a entidades no municipales.',
      recomendacion: 'Se sugiere acudir al juzgado de asuntos municipales, juzgado de paz o a la entidad competente según el caso.'
    }
  ];

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

  // Comparador numérico para ordenamiento por Código (ID)
  readonly comparadorId = {
    compare: (a: any, b: any) => Number(a.id) - Number(b.id)
  };

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

    return list.slice().sort((a, b) => Number(a.id) - Number(b.id));
  });

  ngOnInit(): void {
    if (typeof window !== 'undefined' && window.innerWidth >= 992) {
      this.vistaModo.set('table');
    } else {
      this.vistaModo.set('cards');
    }
    this.cargarIncidencias();

    // Escuchar cambios en queryParam 'id' para abrir el modal directamente
    this.route.queryParamMap.subscribe(params => {
      const targetId = params.get('id');
      if (targetId && this.incidencias().length > 0) {
        const found = this.incidencias().find(i => String(i.id) === targetId);
        if (found && (!this.selectedIncidencia || String(this.selectedIncidencia.id) !== targetId)) {
          this.abrirDetalle(found);
        }
      }
    });
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
        const ordenadas = filtradas.sort((a, b) => Number(a.id) - Number(b.id));
        this.incidencias.set(ordenadas);
        this.loading.set(false);

        // Si se especificó ?id= en la URL (ej. desde el dashboard), abrir directamente su detalle
        const targetId = this.route.snapshot.queryParamMap.get('id');
        if (targetId) {
          const found = filtradas.find(item => String(item.id) === targetId);
          if (found) {
            this.abrirDetalle(found);
          }
        }
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

    // Si es tipo Denuncia y los datos vinieron en testigos por el backend, mapearlos a denunciados
    if (this.isTipoDenuncia(incidencia.tipo) && (!this.selectedIncidencia.denunciados || this.selectedIncidencia.denunciados.length === 0)) {
      if (this.selectedIncidencia.testigos && this.selectedIncidencia.testigos.length > 0) {
        this.selectedIncidencia.denunciados = [...this.selectedIncidencia.testigos];
      }
    }

    this.detalleModalOpen = true;
    this.evidenciasCargadas = [];
    this.cargarResumenesCampo(incidencia.id);

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
    this.resumenesCampo = [];
    this.cargandoResumenes = false;
    this.cerrarPreview();
    if (this.route.snapshot.queryParamMap.has('id')) {
      this.router.navigate([], { relativeTo: this.route, queryParams: { id: null }, queryParamsHandling: 'merge' });
    }
  }

  resumenesCampo: any[] = [];
  cargandoResumenes = false;

  cargarResumenesCampo(incidenciaId: number): void {
    this.cargandoResumenes = true;
    this.resumenesCampo = [];
    this.incidenciasService.getResumenesCampo(incidenciaId).subscribe({
      next: (resumenes) => {
        this.resumenesCampo = resumenes || [];
        if (this.resumenesCampo.length > 0 && this.selectedIncidencia) {
          const ultimo = this.resumenesCampo[this.resumenesCampo.length - 1];
          this.selectedIncidencia.resumen = ultimo.body;
          this.selectedIncidencia.latitudGps = ultimo.latitudGps;
          this.selectedIncidencia.longitudGps = ultimo.longitudGps;
        }
        this.cargandoResumenes = false;
      },
      error: () => {
        this.resumenesCampo = [];
        this.cargandoResumenes = false;
      }
    });
  }

  cargarEvidencias(incidenciaId: number): void {
    this.cargandoEvidencias = true;
    this.incidenciasService.getArchivosIncidenciaAnalista(incidenciaId).subscribe({
      next: (archivos) => {
        // Carga bajo demanda: solo conservamos los metadatos de los archivos (nombre, formato, fecha, id)
        // La descarga real de binarios por SFTP se ejecuta únicamente cuando el analista hace clic en 'Vista Previa' o 'Descargar'
        this.evidenciasCargadas = (archivos || []).map((archivo: any) => ({
          ...archivo,
          blob: null,
          url: null
        }));
        this.cargandoEvidencias = false;
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
      this.mostrarAlerta(`Descargando "${archivo.nombre}" desde el repositorio municipal...`, 'info');
      this.incidenciasService.descargarArchivoIncidencia(this.selectedIncidencia.id, archivo.id).subscribe({
        next: (blob) => {
          if (blob && blob.size > 0) {
            archivo.blob = blob;
            const a = document.createElement('a');
            const objectUrl = URL.createObjectURL(blob);
            a.href = objectUrl;
            a.download = archivo.nombre || 'evidencia_incidencia';
            a.click();
            URL.revokeObjectURL(objectUrl);
          } else {
            this.mostrarAlerta(`El archivo "${archivo.nombre}" no contiene datos en el servidor.`, 'danger');
          }
        },
        error: () => {
          this.mostrarAlerta(`No fue posible descargar el archivo "${archivo.nombre}".`, 'danger');
        }
      });
    }
  }

  // ==========================================
  // FLUJO NORMAL BÁSICO: Aceptar Incidencia (AN1-3)
  // ==========================================

  /**
   * Determina si la incidencia se encuentra lista para dictamen del analista
   * (Incidencia Asignada inicial, Recolección Finalizada por agente de campo, o Apelación Asignada).
   */
  esEstadoDictamenAnalista(incidencia?: any): boolean {
    const inc = incidencia || this.selectedIncidencia;
    if (!inc || !inc.estado) return false;
    const st = (inc.estado || '').toUpperCase();
    const esAsignada = st.includes('ASIGNADA') && !st.includes('CAMPO');
    const esRecoleccionFin = st.includes('RECOLECC');
    const esApelacion = st.includes('APELAC');
    return esAsignada || esRecoleccionFin || esApelacion;
  }

  esEstadoAsignadaAnalista(incidencia?: any): boolean {
    return this.esEstadoDictamenAnalista(incidencia);
  }

  /**
   * El usuario presiona el botón "Aceptar Incidencia"
   * Despliega diálogo de confirmación AN1-3
   */
  solicitarAceptarIncidencia(): void {
    if (!this.esEstadoDictamenAnalista()) {
      this.mostrarAlerta(
        `No es posible dictaminar la incidencia #${this.selectedIncidencia?.id}. Su estado actual es "${this.selectedIncidencia?.estado}".`,
        'danger'
      );
      return;
    }
    this.confirmAceptarOpen = true;
  }

  /**
   * El usuario presiona "Si" en el diálogo de aceptación:
   * Si la recolección de campo ya finalizó, envía confirmación de caso.
   * Si está en asignación inicial o apelación, envía aceptación.
   */
  ejecutarAceptar(): void {
    if (!this.selectedIncidencia || !this.esEstadoDictamenAnalista()) return;

    const id = this.selectedIncidencia.id;
    const st = (this.selectedIncidencia.estado || '').toUpperCase();
    this.procesandoAccion.set(true);

    const call$ = st.includes('RECOLECC')
      ? this.incidenciasService.confirmarIncidencia(id)
      : this.incidenciasService.aceptarIncidencia(id);

    call$.subscribe({
      next: () => {
        // 1. Cierra el diálogo de confirmación
        this.confirmAceptarOpen = false;

        // 2. Cierra el diálogo con la información de la incidencia
        this.detalleModalOpen = false;
        this.selectedIncidencia = null;
        if (this.route.snapshot.queryParamMap.has('id')) {
          this.router.navigate([], { relativeTo: this.route, queryParams: { id: null }, queryParamsHandling: 'merge' });
        }

        // 3. Elimina la solicitud de la tabla
        this.incidencias.update(list => list.filter(item => item.id !== id));

        this.procesandoAccion.set(false);

        // 4. Muestra mensaje en pantalla
        this.mostrarAlerta(`Incidencia #${id} aceptada con éxito. Dictamen municipal registrado correctamente.`, 'success');
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
  // FLUJO ALTERNO FA4: Rechazo de Incidencia con Listado de Feedback (AN1-4)
  // ==========================================

  /**
   * El analista presiona el botón "Rechazar Incidencia" (FA4)
   * Despliega diálogo de confirmación con listado dinámico de motivos y retroalimentación para el vecino
   */
  solicitarRechazarIncidencia(incidencia?: any): void {
    if (incidencia) {
      this.selectedIncidencia = { ...incidencia };
    }
    if (!this.esEstadoDictamenAnalista()) {
      this.mostrarAlerta(
        `No es posible dictaminar el rechazo para la incidencia #${this.selectedIncidencia?.id}. Su estado actual es "${this.selectedIncidencia?.estado}".`,
        'danger'
      );
      return;
    }
    this.listaMotivosRechazo = [];
    this.feedbackTitulo = '';
    this.feedbackDescripcion = '';
    this.feedbackRecomendacion = '';
    this.editandoIndice = null;
    this.formRechazoInvalido = false;
    this.errorListaVacia = false;
    this.confirmRechazarOpen = true;
  }

  /**
   * Aplica una plantilla de causal rápida de rechazo al formulario
   */
  aplicarPlantilla(plantilla: any): void {
    this.feedbackTitulo = plantilla.titulo;
    this.feedbackDescripcion = plantilla.descripcion;
    this.feedbackRecomendacion = plantilla.recomendacion || '';
    this.formRechazoInvalido = false;
    this.errorListaVacia = false;
  }

  /**
   * Agrega un nuevo motivo o guarda la edición del motivo seleccionado en la lista
   */
  agregarMotivo(): void {
    const tit = this.feedbackTitulo.trim();
    const desc = this.feedbackDescripcion.trim();
    const recom = this.feedbackRecomendacion.trim();

    if (!tit || !desc) {
      this.formRechazoInvalido = true;
      return;
    }

    if (this.editandoIndice !== null && this.editandoIndice >= 0) {
      this.listaMotivosRechazo[this.editandoIndice] = {
        titulo: tit,
        descripcion: desc,
        recomendacion: recom || undefined
      };
      this.editandoIndice = null;
    } else {
      this.listaMotivosRechazo.push({
        titulo: tit,
        descripcion: desc,
        recomendacion: recom || undefined
      });
    }

    this.feedbackTitulo = '';
    this.feedbackDescripcion = '';
    this.feedbackRecomendacion = '';
    this.formRechazoInvalido = false;
    this.errorListaVacia = false;
  }

  /**
   * Carga un motivo existente en el formulario para modificarlo
   */
  editarMotivo(index: number): void {
    if (index >= 0 && index < this.listaMotivosRechazo.length) {
      this.editandoIndice = index;
      const m = this.listaMotivosRechazo[index];
      this.feedbackTitulo = m.titulo;
      this.feedbackDescripcion = m.descripcion;
      this.feedbackRecomendacion = m.recomendacion || '';
      this.formRechazoInvalido = false;
    }
  }

  /**
   * Cancela la edición activa de un motivo
   */
  cancelarEdicionMotivo(): void {
    this.editandoIndice = null;
    this.feedbackTitulo = '';
    this.feedbackDescripcion = '';
    this.feedbackRecomendacion = '';
    this.formRechazoInvalido = false;
  }

  /**
   * Elimina un motivo de la lista de rechazo
   */
  eliminarMotivo(index: number): void {
    this.listaMotivosRechazo.splice(index, 1);
    if (this.editandoIndice === index) {
      this.cancelarEdicionMotivo();
    } else if (this.editandoIndice !== null && this.editandoIndice > index) {
      this.editandoIndice--;
    }
  }

  /**
   * Limpia los campos del formulario de redacción
   */
  limpiarFormularioMotivo(): void {
    this.feedbackTitulo = '';
    this.feedbackDescripcion = '';
    this.feedbackRecomendacion = '';
    this.editandoIndice = null;
    this.formRechazoInvalido = false;
  }

  /**
   * El analista confirma el rechazo de la incidencia:
   * 1. Si hay campos redactados pendientes, se integran automáticamente
   * 2. Valida que haya al menos 1 motivo en la lista
   * 3. Envía List<IncidenciaFeedback> al endpoint POST /{id}/event/rechazar-incidencia
   * 4. Remueve la incidencia de la tabla y notifica en pantalla
   */
  ejecutarRechazar(): void {
    if (!this.selectedIncidencia || !this.esEstadoAsignadaAnalista()) return;

    // Si el analista tiene redactado un motivo válido en el formulario, lo incorporamos
    if (this.feedbackTitulo.trim() && this.feedbackDescripcion.trim()) {
      this.agregarMotivo();
    }

    // Debe existir al menos un motivo en el listado
    if (this.listaMotivosRechazo.length === 0) {
      this.errorListaVacia = true;
      this.formRechazoInvalido = true;
      return;
    }

    const id = this.selectedIncidencia.id;
    this.procesandoAccion.set(true);

    const feedbackPayload: IncidenciaFeedback[] = this.listaMotivosRechazo.map(m => ({
      titulo: m.titulo.trim(),
      descripcion: m.descripcion.trim(),
      recomendacion: m.recomendacion?.trim() || undefined
    }));

    this.incidenciasService.rechazarIncidencia(id, feedbackPayload).subscribe({
      next: () => {
        const cant = feedbackPayload.length;
        // Cierra los diálogos y resetea estado
        this.confirmRechazarOpen = false;
        this.detalleModalOpen = false;
        this.selectedIncidencia = null;
        if (this.route.snapshot.queryParamMap.has('id')) {
          this.router.navigate([], { relativeTo: this.route, queryParams: { id: null }, queryParamsHandling: 'merge' });
        }
        this.listaMotivosRechazo = [];
        this.cancelarEdicionMotivo();

        // Elimina de la tabla la solicitud rechazada
        this.incidencias.update(list => list.filter(item => item.id !== id));
        this.procesandoAccion.set(false);

        // Muestra alerta indicando que la retroalimentación fue generada para el vecino
        this.mostrarAlerta(
          `Incidencia #${id} rechazada con éxito (${cant} motivo${cant > 1 ? 's' : ''} registrado${cant > 1 ? 's' : ''} para el vecino). Estado actualizado a "Incidencia Rechazada".`,
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
    this.cancelarEdicionMotivo();
    this.errorListaVacia = false;
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

  esImagenArchivo(archivo: any): boolean {
    const ext = (archivo?.nombre || '').toLowerCase();
    const fmt = (archivo?.formato || '').toLowerCase();
    return /\.(jpg|jpeg|png|gif|webp|svg)$/i.test(ext) || fmt.includes('image') || fmt.includes('jpg') || fmt.includes('png');
  }

  esPdfArchivo(archivo: any): boolean {
    const ext = (archivo?.nombre || '').toLowerCase();
    const fmt = (archivo?.formato || '').toLowerCase();
    return ext.endsWith('.pdf') || fmt.includes('pdf');
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

  getMapsUrl(lat: any, lng: any): string {
    if (!lat || !lng) return '';
    return `https://www.google.com/maps?q=${lat},${lng}`;
  }

  private mostrarAlerta(msg: string, tipo: 'success' | 'danger' | 'info'): void {
    this.alertMessage.set(msg);
    this.alertType.set(tipo);
    setTimeout(() => {
      this.alertMessage.set(null);
    }, 7000);
  }
}

