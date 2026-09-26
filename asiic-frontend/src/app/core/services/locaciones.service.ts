import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { Observable } from "rxjs";
import { LocacionModel } from "../models/locacion.model";

@Injectable({ providedIn: 'root' })
export class LocacionesService {
    private http = inject(HttpClient)
    private baseUrl = `${environment.apiBaseUrl}/public/locaciones`


    getCatalogoPaises(): Observable<LocacionModel[]> {
        return this.http.get<LocacionModel[]>(`${this.baseUrl}/paises`)
    }

    getCatalogoDepartamentos(paisId: number): Observable<LocacionModel[]> {
        return this.http.get<LocacionModel[]>(`${this.baseUrl}/departamentos`, {
            params: {
                pais_id: paisId
            }
        })
    }

    getCatalogoMunicipios(paisId: number, deptoId: number): Observable<LocacionModel[]> {
        return this.http.get<LocacionModel[]>(`${this.baseUrl}/municipios`, {
            params: {
                pais_id: paisId,
                depto_id: deptoId
            }
        })
    }

    getCatalogoComunas(paisId: number, deptoId: number, muniId: number): Observable<LocacionModel[]> {
        return this.http.get<LocacionModel[]>(`${this.baseUrl}/comunas`, {
            params: {
                pais_id: paisId,
                depto_id: deptoId,
                muni_id: muniId
            }
        })
    }
}