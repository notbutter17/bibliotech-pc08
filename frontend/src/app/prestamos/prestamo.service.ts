import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Prestamo } from '../shared/modelos';

@Injectable({ providedIn: 'root' })
export class PrestamoService {
  private readonly http = inject(HttpClient);
  static readonly URL = '/api/prestamos';

  listar(): Observable<Prestamo[]> {
    return this.http.get<Prestamo[]>(PrestamoService.URL);
  }

  registrar(estudianteId: number, libroId: number): Observable<Prestamo> {
    return this.http.post<Prestamo>(PrestamoService.URL, { estudianteId, libroId });
  }

  devolver(prestamoId: number): Observable<Prestamo> {
    return this.http.put<Prestamo>(`${PrestamoService.URL}/${prestamoId}/devolucion`, null);
  }
}
