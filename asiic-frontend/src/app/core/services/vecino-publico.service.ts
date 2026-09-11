import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { ApiResponse, RegistrarVecinoRequest } from '../models/registrar-vecino-request.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class VecinoService {
  private http = inject(HttpClient);

  registrar(body: RegistrarVecinoRequest): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${environment.apiBaseUrl}/public/vecinos`, body)
  }
}
