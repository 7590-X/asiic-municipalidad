import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LocacionDto {
  id: number;
  nombre: string;
}

export interface MuniPayload {
  municipioId: number;
  nombre: string;
  nit: string;
  fechaFundacion?: string;
  direccionFiscal: string;
  latitudGps: string;
  longitudGps: string;
  pbx?: string;
  correo: string;
}

@Injectable({
  providedIn: 'root'
})
export class MunicipalidadService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1';



  registrarMunicipalidad(payload: MuniPayload): Observable<any> {
    return this.http.post(`${this.apiUrl}/asiic/municipalidades`, payload);
  }

  getMunicipalidades(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/asiic/municipalidades`);
  }
}
