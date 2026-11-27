import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PrestamoService } from './prestamo.service';
import { EstudianteService } from './estudiante.service';

describe('PrestamoService y EstudianteService (HttpTestingController)', () => {
  let prestamos: PrestamoService;
  let estudiantes: EstudianteService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    prestamos = TestBed.inject(PrestamoService);
    estudiantes = TestBed.inject(EstudianteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('registrar() hace POST /api/prestamos con estudianteId y libroId', () => {
    prestamos.registrar(1, 2).subscribe();
    const req = http.expectOne('/api/prestamos');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ estudianteId: 1, libroId: 2 });
    req.flush({ id: 5 });
  });

  it('devolver() hace PUT /api/prestamos/{id}/devolucion', () => {
    prestamos.devolver(5).subscribe();
    const req = http.expectOne('/api/prestamos/5/devolucion');
    expect(req.request.method).toBe('PUT');
    req.flush({ id: 5, estado: 'DEVUELTO' });
  });

  it('listar() hace GET /api/prestamos', () => {
    prestamos.listar().subscribe();
    expect(http.expectOne('/api/prestamos').request.method).toBe('GET');
  });

  it('EstudianteService registra (POST) y lista (GET)', () => {
    estudiantes.registrar({ dni: '7', nombre: 'Ana' }).subscribe();
    estudiantes.listar().subscribe();
    const reqs = http.match('/api/estudiantes');
    expect(reqs.map((r) => r.request.method)).toEqual(['POST', 'GET']);
    reqs.forEach((r) => r.flush([]));
  });
});
