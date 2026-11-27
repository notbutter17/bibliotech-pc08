import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Estudiante } from '../shared/modelos';

@Injectable({ providedIn: 'root' })
export class EstudianteService {
  private readonly http = inject(HttpClient);
  static readonly URL = '/api/estudiantes';

  listar(): Observable<Estudiante[]> {
    return this.http.get<Estudiante[]>(EstudianteService.URL);
  }

  registrar(estudiante: Estudiante): Observable<Estudiante> {
    return this.http.post<Estudiante>(EstudianteService.URL, estudiante);
  }
}
