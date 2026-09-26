import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { Observable } from "rxjs";
import { CatalogoItemModel } from "../models/catalogo-item.model";

@Injectable({
    providedIn: 'root'
})
export class CatalogosService {

    private http = inject(HttpClient)
    private baseUrl = `${environment.apiBaseUrl}/public/catalogos`

    getCatalogoEstadoCivil(): Observable<CatalogoItemModel[]> {
        return this.getCatalogos('estado-civil')
    }

    getProfesiones(): Observable<CatalogoItemModel[]> {
        return this.getCatalogos('profesion')
    }

    private getCatalogos(catalogo: string): Observable<CatalogoItemModel[]> {
        return this.http.get<CatalogoItemModel[]>(`${this.baseUrl}/${catalogo}`)
    }
}