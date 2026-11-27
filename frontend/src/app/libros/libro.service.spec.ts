import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LibroService } from './libro.service';
import { Libro } from '../shared/modelos';

describe('LibroService (HttpTestingController)', () => {
  let service: LibroService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(LibroService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('GET /api/libros devuelve el catálogo con su estado', () => {
    // Arrange
    const catalogo: Libro[] = [
      { id: 1, isbn: '978-1', titulo: 'Clean Code', autor: 'R. Martin', ejemplaresDisponibles: 2, estado: 'DISPONIBLE' },
    ];
    let recibido: Libro[] = [];
    // Act
    service.listar().subscribe((l) => (recibido = l));
    const req = http.expectOne('/api/libros');
    req.flush(catalogo);
    // Assert
    expect(req.request.method).toBe('GET');
    expect(recibido).toEqual(catalogo);
  });

  it('POST /api/libros envía el libro en el cuerpo y devuelve el creado', () => {
    // Arrange
    const nuevo: Libro = { isbn: '978-2', titulo: 'TDD', autor: 'K. Beck', ejemplaresDisponibles: 1 };
    let creado: Libro | undefined;
    // Act
    service.registrar(nuevo).subscribe((l) => (creado = l));
    const req = http.expectOne('/api/libros');
    req.flush({ ...nuevo, id: 9, estado: 'DISPONIBLE' }, { status: 201, statusText: 'Created' });
    // Assert
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(nuevo);
    expect(creado?.id).toBe(9);
  });

  it('POST /api/libros propaga el 400 del backend', () => {
    // Arrange
    let status = 0;
    // Act
    service.registrar({ isbn: '', titulo: 'X', autor: 'Y', ejemplaresDisponibles: 1 }).subscribe({
      error: (e) => (status = e.status),
    });
    http.expectOne('/api/libros').flush({ status: 400, mensaje: 'El ISBN es obligatorio' }, { status: 400, statusText: 'Bad Request' });
    // Assert
    expect(status).toBe(400);
  });
});
