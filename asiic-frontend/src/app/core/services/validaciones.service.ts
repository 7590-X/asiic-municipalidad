import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";

@Injectable({
    providedIn: 'root'
})
export class ValidacionesService {

    private http = inject(HttpClient)
    private baseUrl = `${environment.apiBaseUrl}/public/validacion-numeros`

    validacionNumero(key: 'cui' | 'nit' | 'correo', value: string) {
        return this.http.get(`${this.baseUrl}`, {
            params: {
                key: key,
                value: value
            }
        })
    }
}
