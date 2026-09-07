import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { Observable } from "rxjs";
import { CatalogoItem } from "../models/catalogo-item.model";

@Injectable({
    providedIn: 'root'
})
export class CatalogosService {

    private http = inject(HttpClient)
    private baseUrl = `${environment.apiBaseUrl}/public/catalogos`

    getCatalogoEstadoCivil(): Observable<CatalogoItem[]> {
        return this.getCatalogos('estado-civil')
    }

    getProfesiones(): Observable<CatalogoItem[]> {
        return this.getCatalogos('profesion')
    }

    private getCatalogos(catalogo: string): Observable<CatalogoItem[]> {
        return this.http.get<CatalogoItem[]>(`${this.baseUrl}/${catalogo}`)
    }
}