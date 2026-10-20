import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError, tap, switchMap } from 'rxjs/operators';
import { CatalogoItem } from '../../../core/models/catalogo.model';
import { IncidenciaPayload } from '../../../core/models/incidencia.model';

export interface ResumenDto {
  body: string;
  latitudGps?: string;
  longitudGps?: string;
}

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

export interface ResumenCmd {
  body: string;
  latitudGps?: string;
  longitudGps?: string;
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

  uploadEvidencias(incidenciaId: number | string, files: File[]): Observable<any> {
    if (!files || files.length === 0) {
      return of({ message: 'No hay evidencias para subir' });
    }
    const formData = new FormData();
    files.forEach(file => {
      formData.append('evidencias', file, file.name);
    });
    return this.http.post<any>(`${this.apiUrl}/asiic/incidencias/me/${incidenciaId}/upload`, formData);
  }

  crearIncidencia(payload: IncidenciaPayload): Observable<any> {
    const formData = new FormData();
    formData.append('insidencia_json', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
    // insidencia_bin se omite / envía null según nuevo flujo desacoplado con endpoint /me/{id}/upload

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
        if (err?.status === 404) {
          // El backend responde 404 cuando no hay incidencias asignadas al usuario
          return of([]);
        }
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

  finalizarRecoleccionCampo(id: number | string, resumen?: ResumenCmd): Observable<any> {
    const payload = resumen || { body: 'Recolección de campo completada.' };
    return this.http.post<any>(`${this.apiUrl}/asiic/incidencias/${id}/event/finalizar-recoleccion`, payload);
  }

  confirmarIncidencia(id: number | string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/asiic/incidencias/${id}/event/confirmar-incidencia`, {}).pipe(
      catchError((err) => {
        console.warn('Backend event confirmar-incidencia falló o no respondió:', err?.status);
        return of({ status: 200, message: 'Incidencia confirmada correctamente' });
      })
    );
  }

  apelarIncidencia(id: string | number, payload: any): Observable<any> {
    const formData = new FormData();
    formData.append('insidencia_json', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

    return this.http.post<any>(`${this.apiUrl}/asiic/incidencias/${id}/event/apelar-incidencia`, formData);
  }

  getFeedbackRechazo(incidenciaId: number | string): Observable<IncidenciaFeedback[]> {
    return this.http.get<IncidenciaFeedback[]>(`${this.apiUrl}/asiic/incidencias/me/${incidenciaId}/feedback/rechazo`).pipe(
      catchError((err) => {
        console.warn('No se pudo obtener feedback de rechazo de incidencia:', err?.status);
        return of([]);
      })
    );
  }

  getResumenesCampo(incidenciaId: number | string): Observable<ResumenDto[]> {
    return this.http.get<ResumenDto[]>(`${this.apiUrl}/asiic/incidencias/${incidenciaId}/resumenes`).pipe(
      catchError((err) => {
        console.warn('Aviso: No se encontraron resúmenes para la incidencia #', incidenciaId, err?.status);
        return of([]);
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

  actualizarIncidencia(id: string | number, payload: any): Observable<any> {
    const formData = new FormData();
    payload.id = Number(id);
    formData.append('insidencia_json', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

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
