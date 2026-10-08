import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { CatalogoItem } from '../../../core/models/catalogo.model';
import { IncidenciaPayload } from '../../../core/models/incidencia.model';

export interface DomicilioDto {
  contador: string;
  latitud?: string;
  longitud?: string;
  direccion: {
    idDireccion: number;
    descripDireccion: string;
    pais?: string;
    departamento?: string;
    municipio?: string;
    comuna?: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class IncidenciasService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1'; // Ajustar según configuración del proxy/entorno

  getCatalogosDependencias(): Observable<CatalogoItem[]> {
    return this.http.get<CatalogoItem[]>(`${this.apiUrl}/asiic/catalogos/C_DEPENDENCIAS`);
  }

  getMisDomicilios(): Observable<DomicilioDto[]> {
    return this.http.get<DomicilioDto[]>(`${this.apiUrl}/asiic/domicilios/me`).pipe(
      catchError(() => {
        console.warn('Error obteniendo domicilios. Usando lista vacía por ahora.');
        return of([]);
      })
    );
  }

  getCatalogosTiposServicio(): Observable<CatalogoItem[]> {
    return this.http.get<CatalogoItem[]>(`${this.apiUrl}/asiic/catalogos/C_TIPOS_SERVICIO`);
  }

  getCatalogosTiposDenuncia(): Observable<CatalogoItem[]> {
    return this.http.get<CatalogoItem[]>(`${this.apiUrl}/asiic/catalogos/C_TIPOS_DENUNCIA`);
  }

  getCatalogosAreasSugerencia(): Observable<CatalogoItem[]> {
    return this.http.get<CatalogoItem[]>(`${this.apiUrl}/asiic/catalogos/C_AREAS_SUGERENCIA`);
  }

  crearIncidencia(payload: IncidenciaPayload, evidencias: File[]): Observable<any> {
    const formData = new FormData();
    formData.append('insidencia_json', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

    if (evidencias && evidencias.length > 0) {
      evidencias.forEach(file => {
        formData.append('insidencia_bin', file, file.name);
      });
    }

    return this.http.post(`${this.apiUrl}/asiic/incidencias`, formData);
  }


  getMisIncidencias(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/asiic/incidencias/me`).pipe(
      map(res => {
        return res.map(item => ({
          id: item.id,
          tipo: item.tipoIncidencia?.nombre || '',
          asunto: item.descripcion || '',
          dependencia: item.dependencia?.valor || '',
          fecha: item.fechaRegistro ? new Date(item.fechaRegistro).toLocaleDateString() : '',
          estado: item.estado || 'BORRADOR'
        }));
      })
    );
  }

  getIncidenciaById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/asiic/incidencias/me/${id}`);
  }

  getArchivosIncidencia(id: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/asiic/incidencias/me/${id}/archivos`).pipe(
      catchError(() => of([]))
    );
  }

  descargarArchivoIncidencia(incidenciaId: string | number, archivoId: string | number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/asiic/incidencias/me/${incidenciaId}/archivos/${archivoId}`, {
      responseType: 'blob'
    });
  }

  eliminarArchivoIncidencia(incidenciaId: string | number, archivoId: string | number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/asiic/incidencias/me/${incidenciaId}/archivos/${archivoId}`);
  }

  actualizarIncidencia(id: string, payload: any, evidencias: File[]): Observable<any> {
    const formData = new FormData();
    formData.append('insidencia_json', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

    if (evidencias && evidencias.length > 0) {
      evidencias.forEach(file => {
        formData.append('insidencia_bin', file, file.name);
      });
    }

    return this.http.post(`${this.apiUrl}/asiic/incidencias`, formData);
  }
}
