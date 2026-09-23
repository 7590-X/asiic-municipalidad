import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { CatalogoItem } from '../../../core/models/catalogo.model';
import { IncidenciaPayload } from '../../../core/models/incidencia.model';

@Injectable({
  providedIn: 'root'
})
export class IncidenciasService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1'; // Ajustar según configuración del proxy/entorno

  getCatalogosDependencias(): Observable<CatalogoItem[]> {
    return this.http.get<CatalogoItem[]>(`${this.apiUrl}/asiic/public/catalogos/dependencias`);
  }

  getCatalogosTiposServicio(): Observable<CatalogoItem[]> {
    return this.http.get<CatalogoItem[]>(`${this.apiUrl}/asiic/public/catalogos/tipos-servicio`);
  }

  getCatalogosTiposDenuncia(): Observable<CatalogoItem[]> {
    return this.http.get<CatalogoItem[]>(`${this.apiUrl}/asiic/public/catalogos/tipos-denuncia`);
  }

  getCatalogosAreasSugerencia(): Observable<CatalogoItem[]> {
    return this.http.get<CatalogoItem[]>(`${this.apiUrl}/asiic/public/catalogos/areas-sugerencia`);
  }

  crearIncidencia(payload: IncidenciaPayload, evidencias: File[]): Observable<any> {
    const formData = new FormData();
    formData.append('datos', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
    
    if (evidencias && evidencias.length > 0) {
      evidencias.forEach(file => {
        formData.append('evidencias', file, file.name);
      });
    }

    return this.http.post(`${this.apiUrl}/incidencias`, formData);
  }

  // Retorna el perfil del vecino autenticado usando el nuevo endpoint
  getDatosSolicitanteActual(): Observable<any> {
    return this.http.get(`${this.apiUrl}/vecinos/me`);
  }

  getMisIncidencias(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/incidencias`);
  }

  getIncidenciaById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/incidencias/${id}`);
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
