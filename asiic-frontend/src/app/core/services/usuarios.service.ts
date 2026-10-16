import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UsuarioPayload {
  nombres: string;
  apellidos: string;
  correo: string;
  roles: string[];
  municipalidades: number[];
}

@Injectable({
  providedIn: 'root'
})
export class UsuariosService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/asiic/usuarios';

  getRoles(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/roles`);
  }

  getUsuarios(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  crearUsuario(payload: UsuarioPayload): Observable<any> {
    return this.http.post(this.apiUrl, payload);
  }
}
