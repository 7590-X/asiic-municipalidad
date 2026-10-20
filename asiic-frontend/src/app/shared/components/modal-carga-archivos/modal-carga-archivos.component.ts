import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  SimpleChanges,
  inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { IncidenciasService } from '../../../features/incidencias/services/incidencias.service';
import { DragDropDirective } from '../../directives/drag-drop.directive';
import { ModalPreviewArchivoComponent } from '../modal-preview-archivo/modal-preview-archivo.component';

@Component({
  selector: 'app-modal-carga-archivos',
  standalone: true,
  imports: [
    CommonModule,
    ClarityModule,
    DragDropDirective,
    ModalPreviewArchivoComponent
  ],
  templateUrl: './modal-carga-archivos.component.html',
  styleUrls: ['./modal-carga-archivos.component.scss']
})
export class ModalCargaArchivosComponent implements OnChanges {
  private incidenciasService = inject(IncidenciasService);

  @Input() open = false;
  @Output() openChange = new EventEmitter<boolean>();

  @Input() incidenciaId: number | string | null = null;
  @Input() titulo: string = 'Gestión y Carga de Evidencias';
  @Input() soloLectura = false;
  @Output() archivosActualizados = new EventEmitter<any[]>();
  @Output() archivosSeleccionados = new EventEmitter<File[]>();

  // Estados
  cargando = false;
  subiendo = false;
  eliminandoId: number | string | null = null;
  error: string | null = null;
  exito: string | null = null;

  // Archivos
  archivosExistentes: any[] = [];
  nuevosArchivos: File[] = [];

  readonly maxFileSize = 10 * 1024 * 1024; // 10 MB

  // Vista previa modal secundaria
  modalPreviewOpen = false;
  archivoSeleccionado: any = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['open']) {
      if (this.open && this.incidenciaId) {
        this.limpiarEstado();
        this.cargarArchivosExistentes();
      } else if (!this.open) {
        this.limpiarEstado();
      }
    } else if (changes['incidenciaId'] && this.open && this.incidenciaId) {
      this.cargarArchivosExistentes();
    }
  }

  limpiarEstado(): void {
    this.nuevosArchivos = [];
    this.error = null;
    this.exito = null;
    this.subiendo = false;
  }

  cargarArchivosExistentes(): void {
    if (!this.incidenciaId) return;
    this.cargando = true;
    this.incidenciasService.getArchivosIncidencia(this.incidenciaId).subscribe({
      next: (archivos) => {
        this.archivosExistentes = archivos || [];
        this.cargando = false;
        this.archivosActualizados.emit(this.archivosExistentes);
      },
      error: (err) => {
        console.warn('Aviso al obtener archivos de incidencia:', err?.status);
        this.archivosExistentes = [];
        this.cargando = false;
      }
    });
  }

  onFilesDropped(files: File[]): void {
    if (this.soloLectura) return;
    this.procesarArchivos(files);
  }

  onFileSelected(event: any): void {
    if (this.soloLectura) return;
    if (event.target.files && event.target.files.length > 0) {
      this.procesarArchivos(Array.from(event.target.files));
      event.target.value = ''; // Reset input
    }
  }

  private procesarArchivos(files: File[]): void {
    this.error = null;
    this.exito = null;

    for (const file of files) {
      if (file.size > this.maxFileSize) {
        this.error = `El archivo "${file.name}" supera el tamaño máximo permitido de 10 MB.`;
        return;
      }

      const extension = (file.name.split('.').pop() || '').toLowerCase();
      const validExtensions = ['png', 'jpg', 'jpeg', 'pdf', 'webp'];
      if (!validExtensions.includes(extension) && !file.type.match(/image\/(png|jpeg|jpg|webp)|application\/pdf/)) {
        this.error = `El formato del archivo "${file.name}" no está permitido. Solo se aceptan PNG, JPG, JPEG o PDF.`;
        return;
      }

      // Evitar duplicados por nombre y tamaño
      const yaExiste = this.nuevosArchivos.some(f => f.name === file.name && f.size === file.size);
      if (!yaExiste) {
        this.nuevosArchivos.push(file);
      }
    }
    this.archivosSeleccionados.emit(this.nuevosArchivos);
  }

  removerNuevoArchivo(index: number): void {
    this.nuevosArchivos.splice(index, 1);
    this.archivosSeleccionados.emit(this.nuevosArchivos);
  }

  subirArchivos(): void {
    if (this.nuevosArchivos.length === 0 || this.subiendo) return;

    // Si aún no se ha persistido el reporte (incidenciaId es null), se dejan preparados
    if (!this.incidenciaId) {
      this.exito = `${this.nuevosArchivos.length} archivo(s) preparado(s). Se adjuntarán automáticamente al guardar o enviar el reporte.`;
      this.archivosSeleccionados.emit(this.nuevosArchivos);
      return;
    }

    this.subiendo = true;
    this.error = null;
    this.exito = null;

    this.incidenciasService.uploadEvidencias(this.incidenciaId, this.nuevosArchivos).subscribe({
      next: (res) => {
        const count = this.nuevosArchivos.length;
        this.subiendo = false;
        this.nuevosArchivos = [];
        this.archivosSeleccionados.emit([]);
        this.exito = count === 1
          ? 'Evidencia subida exitosamente al expediente.'
          : `${count} evidencias subidas exitosamente al expediente.`;
        this.cargarArchivosExistentes();
      },
      error: (err) => {
        this.subiendo = false;
        console.error('Error al subir evidencias:', err);
        let msg = 'Ocurrió un error al subir los archivos al servidor.';
        if (err?.error?.message) {
          msg = err.error.message;
        }
        this.error = msg;
      }
    });
  }

  eliminarArchivoExistente(archivo: any): void {
    if (this.soloLectura || !this.incidenciaId || !archivo?.id || this.eliminandoId) return;

    const nombre = archivo.nombre || 'este archivo';
    if (!confirm(`¿Está seguro de eliminar "${nombre}" de esta incidencia?`)) {
      return;
    }

    this.eliminandoId = archivo.id;
    this.error = null;
    this.exito = null;

    this.incidenciasService.eliminarArchivoIncidencia(this.incidenciaId, archivo.id).subscribe({
      next: () => {
        this.eliminandoId = null;
        this.exito = `Archivo "${nombre}" eliminado correctamente.`;
        this.archivosExistentes = this.archivosExistentes.filter(a => a.id !== archivo.id);
        this.archivosActualizados.emit(this.archivosExistentes);
      },
      error: (err) => {
        this.eliminandoId = null;
        console.error('Error al eliminar archivo:', err);
        this.error = 'No se pudo eliminar el archivo. Inténtelo nuevamente.';
      }
    });
  }

  previsualizar(archivo: any): void {
    this.archivoSeleccionado = archivo;
    this.modalPreviewOpen = true;
  }

  descargar(archivo: any): void {
    if (!this.incidenciaId || !archivo?.id) return;
    this.incidenciasService.descargarArchivoIncidencia(this.incidenciaId, archivo.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = archivo.nombre || `evidencia_${this.incidenciaId}_${archivo.id}`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Error al descargar archivo:', err);
        alert('No se pudo descargar el archivo.');
      }
    });
  }

  getExtension(nombre?: string): string {
    if (!nombre) return 'DOC';
    const parts = nombre.split('.');
    return parts.length > 1 ? parts.pop()!.toUpperCase() : 'DOC';
  }

  esImagen(archivo: any): boolean {
    const nombre = (archivo?.nombre || '').toLowerCase();
    const formato = (archivo?.formato || '').toLowerCase();
    return !!nombre.match(/\.(png|jpe?g|gif|webp|svg)$/i) || formato.includes('image');
  }

  esPdf(archivo: any): boolean {
    const nombre = (archivo?.nombre || '').toLowerCase();
    const formato = (archivo?.formato || '').toLowerCase();
    return nombre.endsWith('.pdf') || formato.includes('pdf');
  }

  formatearBytes(bytes?: number): string {
    if (!bytes || bytes <= 0) return '';
    const k = 1024;
    const dm = 2;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
  }

  cerrar(): void {
    this.open = false;
    this.openChange.emit(false);
  }
}
