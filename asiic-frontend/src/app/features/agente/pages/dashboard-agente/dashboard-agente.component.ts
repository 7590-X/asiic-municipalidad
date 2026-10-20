import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ClarityModule } from '@clr/angular';
import { QuillEditorComponent } from 'ngx-quill';
import { AuthService } from '../../../../core/services/auth.service';
import { IncidenciasService, ResumenCmd } from '../../../incidencias/services/incidencias.service';
import { ModalPreviewArchivoComponent } from '../../../../shared/components/modal-preview-archivo/modal-preview-archivo.component';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard-agente',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    ClarityModule,
    QuillEditorComponent,
    ModalPreviewArchivoComponent
  ],
  templateUrl: './dashboard-agente.component.html',
  styleUrls: ['./dashboard-agente.component.scss']
})
export class DashboardAgenteComponent implements OnInit {
  private authService = inject(AuthService);
  private incidenciasService = inject(IncidenciasService);
  private fb = inject(FormBuilder);

  loading = signal<boolean>(true);
  procesandoAccion = signal<boolean>(false);
  incidencias = signal<any[]>([]);

  // Filtros reactivos
  filtroBusqueda = signal<string>('');
  filtroEstado = signal<string>('TODOS');

  // Modal de Detalle / Inspección
  modalDetalleOpen = false;
  selectedIncidencia: any = null;
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

  // Diálogo de confirmación para finalizar recolección
  confirmFinalizarOpen = false;
  incidenciaAFinalizar: any = null;

  // Alertas
  alertMessage = signal<string | null>(null);
  alertType = signal<'success' | 'danger' | 'info'>('info');

  readonly currentUser = computed(() => this.authService.currentUser());

  // Métricas de campo
  readonly totalAsignadas = computed(() => this.incidencias().length);

  readonly totalPendientesCampo = computed(() =>
    this.incidencias().filter(i => {
      const st = (i.estado || '').toUpperCase();
      return st.includes('CAMPO') || st === 'ASIGNADA_CAMPO';
    }).length
  );

  readonly totalRecoleccionFin = computed(() =>
    this.incidencias().filter(i => {
      const st = (i.estado || '').toUpperCase();
      return st.includes('RECOLECC') || st === 'RECOLECCION_FIN';
    }).length
  );

  readonly totalEnProceso = computed(() =>
    this.incidencias().filter(i => {
      const st = (i.estado || '').toUpperCase();
      return st.includes('ACEPTADA') || st.includes('CONFIRMADA') || st.includes('SOLUCION');
    }).length
  );

  // Lista filtrada
  readonly incidenciasFiltradas = computed(() => {
    let list = this.incidencias();

    const q = this.filtroBusqueda().trim().toLowerCase();
    if (q) {
      list = list.filter(i =>
        String(i.id).includes(q) ||
        (i.tipo || '').toLowerCase().includes(q) ||
        (i.asunto || '').toLowerCase().includes(q) ||
        (i.nombreVecino || '').toLowerCase().includes(q) ||
        (i.contador || '').toLowerCase().includes(q) ||
        (i.direccion || '').toLowerCase().includes(q)
      );
    }

    const estado = this.filtroEstado().toUpperCase();
    if (estado !== 'TODOS') {
      list = list.filter(i => (i.estado || '').toUpperCase().includes(estado));
    }

    return list;
  });

  ngOnInit(): void {
    this.initResumenForm();
    this.cargarDatos();
  }

  initResumenForm(): void {
    this.resumenForm = this.fb.group({
      body: ['', [Validators.required, Validators.minLength(10)]],
      latitudGps: [''],
      longitudGps: ['']
    });
  }

  cargarDatos(): void {
    this.loading.set(true);
    this.incidenciasService.getIncidenciasAsignadas().subscribe({
      next: (data) => {
        this.incidencias.set(data || []);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando incidencias asignadas al agente de campo:', err);
        this.loading.set(false);
      }
    });
  }

  abrirDetalle(inc: any): void {
    this.selectedIncidencia = { ...inc };
    this.modalDetalleOpen = true;
    this.cargarEvidencias(inc.id);
  }

  cerrarDetalle(): void {
    this.modalDetalleOpen = false;
    this.selectedIncidencia = null;
    this.evidenciasCargadas = [];
    this.cerrarPreview();
  }

  cargarEvidencias(incidenciaId: number): void {
    this.cargandoEvidencias = true;
    this.incidenciasService.getArchivosIncidenciaAnalista(incidenciaId).subscribe({
      next: (archivos) => {
        // Carga ligera: solo metadatos. El binario se descarga bajo demanda al previsualizar o descargar.
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
      const objectUrl = URL.createObjectURL(archivo.blob);
      window.open(objectUrl, '_blank');
    } else if (archivo.url) {
      window.open(archivo.url, '_blank');
    } else if (this.selectedIncidencia?.id && archivo.id) {
      this.incidenciasService.descargarArchivoIncidencia(this.selectedIncidencia.id, archivo.id).subscribe({
        next: (blob) => {
          if (blob && blob.size > 0) {
            const isPdf = archivo.nombre?.toLowerCase().endsWith('.pdf');
            const isImage = /\.(png|jpe?g|gif|webp)$/i.test(archivo.nombre || '');
            let mimeType = blob.type || archivo.formato || '';
            if (isPdf && !mimeType) mimeType = 'application/pdf';
            if (isImage && !mimeType) mimeType = 'image/jpeg';

            const typedBlob = mimeType ? new Blob([blob], { type: mimeType }) : blob;
            const objectUrl = URL.createObjectURL(typedBlob);

            // Abre directamente en nueva pestaña para ver fotos o PDFs
            const newWindow = window.open(objectUrl, '_blank');
            if (!newWindow) {
              const a = document.createElement('a');
              a.href = objectUrl;
              a.download = archivo.nombre || 'evidencia_incidencia';
              a.click();
            }
          } else {
            this.mostrarAlerta(`El archivo "${archivo.nombre}" no contiene datos.`, 'info');
          }
        },
        error: () => {
          this.mostrarAlerta(`Error al cargar el archivo "${archivo.nombre}".`, 'danger');
        }
      });
    }
  }

  resumenForm!: FormGroup;
  obteniendoGps = signal<boolean>(false);

  quillModules = {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'],
      [{ 'header': [1, 2, 3, false] }],
      [{ 'list': 'ordered' }, { 'list': 'bullet' }],
      ['blockquote'],
      ['clean']
    ]
  };

  solicitarFinalizarRecoleccion(inc: any): void {
    this.incidenciaAFinalizar = inc;
    const lat = inc.latitud || inc.latitudGps || '';
    const lng = inc.longitud || inc.longitudGps || '';
    this.resumenForm.reset({
      body: '',
      latitudGps: lat,
      longitudGps: lng
    });
    this.confirmFinalizarOpen = true;
  }

  capturarGpsActual(): void {
    if (!navigator.geolocation) {
      alert('La geolocalización no está soportada por su navegador.');
      return;
    }
    this.obteniendoGps.set(true);
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude.toFixed(6);
        const lng = position.coords.longitude.toFixed(6);
        this.resumenForm.patchValue({
          latitudGps: lat,
          longitudGps: lng
        });
        this.obteniendoGps.set(false);
      },
      (error) => {
        console.warn('Error capturando GPS:', error);
        alert('No se pudo obtener la ubicación GPS automática. Ingrese las coordenadas manualmente si lo desea.');
        this.obteniendoGps.set(false);
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );
  }

  ejecutarFinalizarRecoleccion(): void {
    if (!this.incidenciaAFinalizar) return;

    if (this.resumenForm.invalid) {
      this.resumenForm.markAllAsTouched();
      alert('Por favor redacte el informe técnico de campo en el editor antes de finalizar la recolección.');
      return;
    }

    const id = this.incidenciaAFinalizar.id;
    const formVal = this.resumenForm.getRawValue();

    const payload: ResumenCmd = {
      body: formVal.body ? formVal.body.trim() : '',
      latitudGps: formVal.latitudGps ? String(formVal.latitudGps).trim() : undefined,
      longitudGps: formVal.longitudGps ? String(formVal.longitudGps).trim() : undefined
    };

    this.procesandoAccion.set(true);

    this.incidenciasService.finalizarRecoleccionCampo(id, payload).subscribe({
      next: () => {
        // Actualizar reactivamente
        this.incidencias.update(list =>
          list.map(item => item.id === id ? { ...item, estado: 'Recolección Finalizada' } : item)
        );

        if (this.selectedIncidencia && this.selectedIncidencia.id === id) {
          this.selectedIncidencia.estado = 'Recolección Finalizada';
        }

        this.procesandoAccion.set(false);
        this.confirmFinalizarOpen = false;
        this.incidenciaAFinalizar = null;
        this.resumenForm.reset();
        this.mostrarAlerta(
          `Inspección en sitio y reporte técnico finalizados para la incidencia #${id}. El caso avanzó a "Recolección Finalizada".`,
          'success'
        );
      },
      error: (err) => {
        console.error('Error al finalizar recolección', err);
        this.procesandoAccion.set(false);
        let msg = `Error al registrar la finalización de la inspección #${id}.`;
        if (err?.error?.message) {
          msg = err.error.message;
        }
        this.mostrarAlerta(msg, 'danger');
      }
    });
  }

  cancelarConfirmacion(): void {
    this.confirmFinalizarOpen = false;
    this.incidenciaAFinalizar = null;
  }

  getMapsUrl(lat: any, lng: any): string {
    if (!lat || !lng) return '';
    return `https://www.google.com/maps?q=${lat},${lng}`;
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
    if (st.includes('CAMPO')) return 'badge-blue';
    if (st.includes('RECOLECC')) return 'badge-info';
    if (st.includes('ACEPTADA')) return 'badge-success';
    if (st.includes('CONFIRMADA')) return 'badge-success';
    if (st.includes('RECHAZADA')) return 'badge-danger';
    return 'badge-warning';
  }

  private mostrarAlerta(msg: string, tipo: 'success' | 'danger' | 'info'): void {
    this.alertMessage.set(msg);
    this.alertType.set(tipo);
    setTimeout(() => {
      this.alertMessage.set(null);
    }, 7000);
  }
}
