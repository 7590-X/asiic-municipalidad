import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, of, shareReplay, tap, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CatalogoItem } from '../models/catalogo-item.model';

const CACHE_PREFIX = 'asiic:catalogo:';
const TTL_MS = 30 * 60 * 1000; // 30 minutos

interface EntradaCache {
  data: CatalogoItem[];
  guardadoEn: number; // epoch ms
}

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/public/catalogos`;

  private cache = new Map<string, Observable<CatalogoItem[]>>();

  private obtener(path: string): Observable<CatalogoItem[]> {
    let obs = this.cache.get(path);
    if (obs) return obs;

    const guardado = this.leerStorage(path);

    obs = guardado
      ? of(guardado).pipe(shareReplay({ bufferSize: 1, refCount: false }))
      : this.http.get<CatalogoItem[]>(`${this.base}/${path}`).pipe(
        tap((data) => this.guardarStorage(path, data)),
        catchError((err) => {
          this.cache.delete(path);
          return throwError(() => err);
        }),
        shareReplay({ bufferSize: 1, refCount: false }),
      );

    this.cache.set(path, obs);
    return obs;
  }

  private leerStorage(path: string): CatalogoItem[] | null {
    try {
      const raw = localStorage.getItem(CACHE_PREFIX + path);
      if (!raw) return null;

      const entrada = JSON.parse(raw) as EntradaCache;
      const vencido = Date.now() - entrada.guardadoEn > TTL_MS;
      if (vencido) {
        localStorage.removeItem(CACHE_PREFIX + path);
        return null; // fuerza a pedirlo de nuevo al backend
      }
      return entrada.data;
    } catch {
      return null;
    }
  }

  private guardarStorage(path: string, data: CatalogoItem[]): void {
    try {
      const entrada: EntradaCache = { data, guardadoEn: Date.now() };
      localStorage.setItem(CACHE_PREFIX + path, JSON.stringify(entrada));
    } catch {
      /* storage lleno o bloqueado: no rompe la app */
    }
  }

  estadoCivil(): Observable<CatalogoItem[]> { return this.obtener('estado-civil'); }
  profesiones(): Observable<CatalogoItem[]> { return this.obtener('profesion'); }
  zonas(): Observable<CatalogoItem[]> { return this.obtener('zonas'); }

  limpiarCache(): void {
    this.cache.clear();
    try {
      ['estado-civil', 'profesion', 'zonas'].forEach((p) =>
        localStorage.removeItem(CACHE_PREFIX + p),
      );
    } catch {
      /* no-op */
    }
  }
}
