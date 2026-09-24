import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { CatalogoItem } from '../../../core/models/catalogo.model';
import { IncidenciaPayload } from '../../../core/models/incidencia.model';

export interface ContadorVecino {
  do_contador: string;
  do_latitud?: string;
  do_longitud?: string;
  do_direccion: {
    di_id: number;
    di_direccion: string;
    di_locacion?: {
      lo_id: number;
      lo_descripcion: string;
      lo_zipcode?: string;
    };
  };
}

@Injectable({
  providedIn: 'root'
})
export class IncidenciasService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1'; // Ajustar según configuración del proxy/entorno

  getCatalogosDependencias(): Observable<CatalogoItem[]> {
    return this.http.get<CatalogoItem[]>(`${this.apiUrl}/asiic/catalogos/dependencias`);
  }

  getMisContadores(): Observable<ContadorVecino[]> {
    return this.http.get<ContadorVecino[]>(`${this.apiUrl}/asiic/incidencias/contadores`).pipe(
      catchError(() => {
        console.warn('El endpoint de contadores aún no está listo. Usando lista vacía por ahora.');
        return of([]);
      })
    );
  }

  getCatalogosTiposServicio(): Observable<CatalogoItem[]> {
    return this.http.get<CatalogoItem[]>(`${this.apiUrl}/asiic/catalogos/tipos-servicio`);
  }

  getCatalogosTiposDenuncia(): Observable<CatalogoItem[]> {
    return this.http.get<CatalogoItem[]>(`${this.apiUrl}/asiic/catalogos/tipos-denuncia`);
  }

  getCatalogosAreasSugerencia(): Observable<CatalogoItem[]> {
    return this.http.get<CatalogoItem[]>(`${this.apiUrl}/asiic/catalogos/areas-sugerencia`);
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
    return this.http.get<any[]>(`${this.apiUrl}/asiic/incidencias`).pipe(
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
    return this.http.get<any>(`${this.apiUrl}/asiic/incidencias/${id}`);
  }

  actualizarIncidencia(id: string, payload: any, evidencias: File[]): Observable<any> {
    const formData = new FormData();
    formData.append('datos', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

    if (evidencias && evidencias.length > 0) {
      evidencias.forEach(file => {
        formData.append('evidencias', file, file.name);
      });
    }

    return this.http.put(`${this.apiUrl}/incidencias/${id}`, formData);
  }
}
