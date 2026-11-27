import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Libro } from '../shared/modelos';

@Injectable({ providedIn: 'root' })
export class LibroService {
  private readonly http = inject(HttpClient);
  static readonly URL = '/api/libros';

  listar(): Observable<Libro[]> {
    return this.http.get<Libro[]>(LibroService.URL);
  }

  registrar(libro: Libro): Observable<Libro> {
    return this.http.post<Libro>(LibroService.URL, libro);
  }
}
