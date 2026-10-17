import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { IncidenciasService } from '../../../features/incidencias/services/incidencias.service';

@Component({
  selector: 'app-modal-preview-archivo',
  standalone: true,
  imports: [CommonModule, ClarityModule],
  templateUrl: './modal-preview-archivo.component.html',
  styleUrls: ['./modal-preview-archivo.component.scss']
})
export class ModalPreviewArchivoComponent implements OnChanges, OnDestroy {
  private sanitizer = inject(DomSanitizer);
  private incidenciasService = inject(IncidenciasService);

  @Input() open = false;
  @Output() openChange = new EventEmitter<boolean>();

  @Input() archivo: any = null;
  @Input() incidenciaId: number | string | null = null;
  @Output() descargar = new EventEmitter<any>();

  cargando = false;
  error: string | null = null;

  tipoArchivo: 'image' | 'pdf' | 'otro' = 'otro';
  previewSafeUrl: SafeResourceUrl | null = null;
  rawBlobUrl: string | null = null;
  private createdInternalBlobUrl: string | null = null;

  // Controles de visualización de imagen
  zoomLevel = 1.0;
  rotation = 0;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['open']) {
      if (this.open && this.archivo) {
        this.cargarVistaPrevia();
      } else if (!this.open) {
        this.limpiarEstado();
      }
    } else if (changes['archivo'] && this.open) {
      this.cargarVistaPrevia();
    }
  }

  ngOnDestroy(): void {
    this.limpiarEstado();
  }

  cargarVistaPrevia(): void {
    this.limpiarEstado(false);

    if (!this.archivo) {
      this.error = 'No se especificó un archivo para previsualizar.';
      return;
    }

    const nombre = (this.archivo.nombre || '').toLowerCase();
    const formato = (this.archivo.formato || '').toLowerCase();

    // Detectar tipo de archivo
    if (nombre.match(/\.(png|jpe?g|gif|webp|svg)$/i) || formato.includes('image')) {
      this.tipoArchivo = 'image';
    } else if (nombre.endsWith('.pdf') || formato.includes('pdf')) {
      this.tipoArchivo = 'pdf';
    } else {
      this.tipoArchivo = 'otro';
    }

    // Caso 1: El archivo ya tiene un Blob en memoria
    if (this.archivo.blob && this.archivo.blob.size > 0) {
      this.establecerBlob(this.archivo.blob, nombre, formato);
      return;
    }

    // Caso 2: El archivo ya tiene una URL establecida
    if (this.archivo.url) {
      this.rawBlobUrl = this.archivo.url;
      this.previewSafeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.archivo.url);
      this.cargando = false;
      return;
    }

    // Caso 3: Es necesario descargar el blob desde el servidor
    if (this.incidenciaId && this.archivo.id) {
      this.cargando = true;
      this.incidenciasService.descargarArchivoIncidencia(this.incidenciaId, this.archivo.id).subscribe({
        next: (blob) => {
          this.cargando = false;
          if (blob && blob.size > 0) {
            // Cachear en el objeto del archivo para que futuras aperturas sean instantáneas
            this.archivo.blob = blob;
            this.establecerBlob(blob, nombre, formato);
          } else {
            this.error = 'El archivo no contiene datos en el servidor.';
          }
        },
        error: (err) => {
          this.cargando = false;
          console.error('Error cargando evidencia para vista previa:', err);
          this.error = 'No fue posible cargar el archivo para vista previa desde el servidor.';
        }
      });
      return;
    }

    this.error = 'No se encontraron datos disponibles para previsualizar este archivo.';
  }

  private establecerBlob(blob: Blob, nombre: string, formato: string): void {
    const mime = this.obtenerMimeType(nombre, formato);
    const typedBlob = new Blob([blob], { type: mime });
    const url = URL.createObjectURL(typedBlob);
    this.createdInternalBlobUrl = url;
    this.rawBlobUrl = url;
    this.previewSafeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
    this.cargando = false;
  }

  private obtenerMimeType(nombre: string, formato: string): string {
    const ext = (nombre || '').split('.').pop()?.toLowerCase() || '';
    switch (ext) {
      case 'pdf': return 'application/pdf';
      case 'png': return 'image/png';
      case 'jpg':
      case 'jpeg': return 'image/jpeg';
      case 'gif': return 'image/gif';
      case 'webp': return 'image/webp';
      case 'svg': return 'image/svg+xml';
      case 'txt': return 'text/plain';
      default:
        if (formato.includes('pdf')) return 'application/pdf';
        if (formato.includes('png')) return 'image/png';
        if (formato.includes('jpeg') || formato.includes('jpg')) return 'image/jpeg';
        return 'application/octet-stream';
    }
  }

  // Zoom y rotación de imágenes
  zoomIn(): void {
    if (this.zoomLevel < 3.0) {
      this.zoomLevel = +(this.zoomLevel + 0.25).toFixed(2);
    }
  }

  zoomOut(): void {
    if (this.zoomLevel > 0.5) {
      this.zoomLevel = +(this.zoomLevel - 0.25).toFixed(2);
    }
  }

  zoomReset(): void {
    this.zoomLevel = 1.0;
    this.rotation = 0;
  }

  rotar(): void {
    this.rotation = (this.rotation + 90) % 360;
  }

  toggleZoom(): void {
    if (this.zoomLevel === 1.0) {
      this.zoomLevel = 1.75;
    } else {
      this.zoomLevel = 1.0;
    }
  }

  abrirEnPestana(): void {
    if (this.rawBlobUrl) {
      window.open(this.rawBlobUrl, '_blank');
    }
  }

  descargarArchivo(): void {
    if (this.descargar.observed) {
      this.descargar.emit(this.archivo);
      return;
    }

    if (this.rawBlobUrl) {
      const a = document.createElement('a');
      a.href = this.rawBlobUrl;
      a.download = this.archivo.nombre || 'evidencia_incidencia';
      a.click();
    }
  }

  cerrar(): void {
    this.open = false;
    this.openChange.emit(false);
    this.limpiarEstado();
  }

  private limpiarEstado(revocarUrl = true): void {
    if (revocarUrl && this.createdInternalBlobUrl) {
      URL.revokeObjectURL(this.createdInternalBlobUrl);
      this.createdInternalBlobUrl = null;
    }
    this.cargando = false;
    this.error = null;
    this.previewSafeUrl = null;
    this.rawBlobUrl = null;
    this.zoomLevel = 1.0;
    this.rotation = 0;
  }

  getExtension(nombre?: string): string {
    if (!nombre) return 'ARCHIVO';
    const partes = nombre.split('.');
    return partes.length > 1 ? partes.pop()!.toUpperCase() : 'ARCHIVO';
  }
}
