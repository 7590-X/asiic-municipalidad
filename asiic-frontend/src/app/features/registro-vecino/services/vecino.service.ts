import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { RegistrarVecinoRequest } from '../models/registrar-vecino.model';

@Injectable({ providedIn: 'root' })
export class VecinoService {
  private http = inject(HttpClient);

  registrar(body: RegistrarVecinoRequest) {
    // 201 => texto plano + header Location ; error => ApiResponseDto (JSON como string)
    return this.http.post(`${environment.apiBaseUrl}/public/vecinos`, body, {
      observe: 'response',
      responseType: 'text',
    });
  }
}
