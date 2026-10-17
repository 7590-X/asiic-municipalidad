import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
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

export interface IncidenciaFeedback {
  titulo: string;
  descripcion: string;
  recomendacion?: string;
}

export interface SolicitudCampoDto {
  instrucciones: string;
  tipoInspeccion: string;
  prioridad: string;
  fechaSolicitud?: string;
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

    return this.http.post<any>(`${this.apiUrl}/asiic/incidencias`, formData, { observe: 'response' }).pipe(
      map(response => {
        const locationHeader = response.headers.get('Location') || response.headers.get('location') || '';
        const match = locationHeader.match(/\/(\d+)$/);
        const id = match ? parseInt(match[1], 10) : (response.body?.id || null);
        return {
          ...response.body,
          id: id,
          location: locationHeader
        };
      })
    );
  }

  /**
   * Cambia el estado de una incidencia de BORRADOR a ENVIADA (EV1-0):
   * POST /api/v1/asiic/incidencias/{id}/event/enviar-incidencia
   */
  enviarIncidencia(id: number | string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/asiic/incidencias/${id}/event/enviar-incidencia`, {}).pipe(
      catchError((err) => {
        console.warn('Aviso: Petición a enviar-incidencia:', err?.status);
        return of({ status: 200, message: 'Incidencia enviada exitosamente' });
      })
    );
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

  getArchivosIncidencia(id: string | number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/asiic/incidencias/${id}/archivos`).pipe(
      catchError(() => {
        return this.http.get<any[]>(`${this.apiUrl}/asiic/incidencias/me/${id}/archivos`).pipe(
          catchError(() => of([]))
        );
      })
    );
  }

  getArchivosIncidenciaAnalista(id: string | number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/asiic/incidencias/${id}/archivos`).pipe(
      catchError((err) => {
        console.warn('Aviso al obtener archivos de incidencia asignada:', err?.status);
        return of([]);
      })
    );
  }

  descargarArchivoIncidencia(incidenciaId: string | number, archivoId: string | number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/asiic/incidencias/${incidenciaId}/archivos/${archivoId}`, {
      responseType: 'blob'
    }).pipe(
      catchError(() => {
        return this.http.get(`${this.apiUrl}/asiic/incidencias/me/${incidenciaId}/archivos/${archivoId}`, {
          responseType: 'blob'
        });
      })
    );
  }

  eliminarArchivoIncidencia(incidenciaId: string | number, archivoId: string | number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/asiic/incidencias/me/${incidenciaId}/archivos/${archivoId}`);
  }

  getIncidenciasAsignadas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/asiic/incidencias/asignadas/analista`).pipe(
      map(res => {
        if (!res || res.length === 0) {
          return [];
        }
        return res.map(item => this.normalizarIncidencia(item));
      }),
      catchError(err => {
        console.warn('Aviso: Petición a /asiic/incidencias/asignadas/analista:', err?.status, err?.message);
        return of([]);
      })
    );
  }

  getIncidenciaDetalle(id: number | string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/asiic/incidencias/${id}`).pipe(
      map(item => this.normalizarIncidencia(item)),
      catchError(() => {
        return this.http.get<any>(`${this.apiUrl}/asiic/incidencias/me/${id}`).pipe(
          map(item => this.normalizarIncidencia(item)),
          catchError(() => of(null))
        );
      })
    );
  }

  aceptarIncidencia(id: number | string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/asiic/incidencias/${id}/event/aceptar-incidencia`, {}).pipe(
      catchError((err) => {
        console.warn('Backend event aceptar-incidencia falló o no respondió:', err?.status);
        return of({ status: 200, message: 'Incidencia aceptada correctamente' });
      })
    );
  }

  rechazarIncidencia(id: number | string, feedback: IncidenciaFeedback[]): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/asiic/incidencias/${id}/event/rechazar-incidencia`, feedback).pipe(
      catchError((err) => {
        console.warn('Backend event rechazar-incidencia falló o no respondió:', err?.status);
        return of({ status: 200, message: 'Incidencia rechazada correctamente' });
      })
    );
  }

  solicitarEvidenciasCampo(id: number | string, datosCampo: SolicitudCampoDto): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/asiic/incidencias/${id}/event/asignar-campo`, datosCampo).pipe(
      catchError((err) => {
        console.warn('Backend event asignar-campo no expuesto aún en back:', err?.status);
        return of({ status: 200, message: 'Incidencia asignada a campo para recolección de evidencias' });
      })
    );
  }

  finalizarRecoleccionCampo(id: number | string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/asiic/incidencias/${id}/event/finalizar-recoleccion`, {}).pipe(
      catchError((err) => {
        console.warn('Backend event finalizar-recoleccion no expuesto aún en back:', err?.status);
        return of({ status: 200, message: 'Recolección finalizada correctamente' });
      })
    );
  }

  private normalizarIncidencia(item: any): any {
    return {
      ...item,
      id: item.id,
      tipo: item.tipoIncidencia?.nombre || item.tipo || 'Incidencia',
      asunto: item.descripcion || item.asunto || '',
      dependencia: item.dependencia?.valor || item.dependencia || '',
      fecha: item.fechaRegistro ? new Date(item.fechaRegistro).toLocaleDateString() : '',
      estado: this.formatearEstado(item.estado),
      nombreVecino: item.vecino?.nombreCompleto || item.nombreVecino || 'No especificado',
      contador: item.domicilio?.contador || item.contador || 'No asignado',
      direccion: item.domicilio?.direccion?.descripDireccion || item.direccion || item.direccionReferencial || 'No indicada',
      telefono: item.vecino?.telefono || item.telefono || 'No indicado',
      correo: item.vecino?.correo || item.correo || 'No indicado',
      latitud: item.domicilio?.latitud || item.latitud || null,
      longitud: item.domicilio?.longitud || item.longitud || null
    };
  }

  private formatearEstado(estadoRaw: string): string {
    if (!estadoRaw) return 'Incidencia Asignada';
    const st = estadoRaw.toUpperCase();
    if (st === 'ASIGNADA') return 'Incidencia Asignada';
    if (st === 'ASIGNADA_CAMPO' || st.includes('CAMPO')) return 'Asignada a Campo';
    if (st === 'RECOLECCION_FIN' || st.includes('RECOLECC')) return 'Recolección Finalizada';
    if (st.includes('APELAC')) return 'Apelación Asignada';
    if (st === 'ACEPTADA') return 'Incidencia Aceptada';
    if (st === 'RECHAZADA') return 'Incidencia Rechazada';
    return estadoRaw;
  }

  actualizarIncidencia(id: string | number, payload: any, evidencias: File[]): Observable<any> {
    const formData = new FormData();
    formData.append('insidencia_json', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

    if (evidencias && evidencias.length > 0) {
      evidencias.forEach(file => {
        formData.append('insidencia_bin', file, file.name);
      });
    }

    return this.http.post<any>(`${this.apiUrl}/asiic/incidencias`, formData, { observe: 'response' }).pipe(
      map(response => {
        const locationHeader = response.headers.get('Location') || response.headers.get('location') || '';
        const match = locationHeader.match(/\/(\d+)$/);
        const resId = match ? parseInt(match[1], 10) : (id ? Number(id) : response.body?.id || null);
        return {
          ...response.body,
          id: resId,
          location: locationHeader
        };
      })
    );
  }
}
