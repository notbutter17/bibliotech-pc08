import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PrestamosComponent } from './prestamos.component';
import { Prestamo } from '../shared/modelos';

describe('PrestamosComponent (Pantalla 2)', () => {
  let fixture: ComponentFixture<PrestamosComponent>;
  let component: PrestamosComponent;
  let http: HttpTestingController;
  let html: HTMLElement;

  const libro = { id: 1, isbn: '978-1', titulo: 'Clean Code', autor: 'R. Martin', ejemplaresDisponibles: 2, estado: 'DISPONIBLE' as const };
  const ana = { id: 1, dni: '70123456', nombre: 'Ana', estado: 'ACTIVO' as const };
  const prestamoActivo: Prestamo = { id: 7, estudiante: ana, libro: { ...libro, ejemplaresDisponibles: 1 }, fechaPrestamo: '2026-11-27', estado: 'ACTIVO' };

  /** Responde las tres cargas iniciales (libros, estudiantes, préstamos). */
  function responderCargas(prestamos: Prestamo[] = []): void {
    http.expectOne('/api/libros').flush([libro]);
    http.expectOne((r) => r.method === 'GET' && r.url === '/api/estudiantes').flush([ana]);
    http.expectOne((r) => r.method === 'GET' && r.url === '/api/prestamos').flush(prestamos);
  }

  async function render(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrestamosComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    fixture = TestBed.createComponent(PrestamosComponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    html = fixture.nativeElement;
    fixture.detectChanges();
    responderCargas([prestamoActivo]);
    await render();
  });

  afterEach(() => http.verify());

  it('carga existencias y préstamos con botón Devolver para los ACTIVOS', () => {
    expect(html.querySelector('[data-cy=ejemplares]')?.textContent).toBe('2');
    expect(html.querySelectorAll('[data-cy=btn-devolver]').length).toBe(1);
  });

  it('RN-03 · registra un préstamo y muestra los ejemplares restantes', async () => {
    component.estudianteId = 1;
    component.libroId = 1;
    component.registrarPrestamo();
    const req = http.expectOne((r) => r.method === 'POST');
    expect(req.request.body).toEqual({ estudianteId: 1, libroId: 1 });
    req.flush(prestamoActivo);
    responderCargas([prestamoActivo]);
    await render();
    expect(html.querySelector('[data-cy=mensaje]')?.textContent).toContain('Quedan 1 ejemplares');
  });

  it('RN-03 · muestra el rechazo del backend (409 sin ejemplares)', async () => {
    component.estudianteId = 1;
    component.libroId = 1;
    component.registrarPrestamo();
    http.expectOne((r) => r.method === 'POST').flush(
      { status: 409, mensaje: 'El libro no tiene ejemplares disponibles' },
      { status: 409, statusText: 'Conflict' },
    );
    await render();
    const alerta = html.querySelector('[data-cy=mensaje]');
    expect(alerta?.textContent).toContain('El libro no tiene ejemplares disponibles');
    expect(alerta?.classList).toContain('ko');
  });

  it('no envía el préstamo si falta seleccionar estudiante o libro', async () => {
    component.estudianteId = null;
    component.registrarPrestamo();
    await render();
    expect(component.mensaje()).toBe('Selecciona un estudiante y un libro.');
    http.expectNone((r) => r.method === 'POST');
  });

  it('RN-04 · devolver hace PUT y muestra el mensaje', async () => {
    (html.querySelector('[data-cy=btn-devolver]') as HTMLButtonElement).click();
    http.expectOne('/api/prestamos/7/devolucion').flush({ ...prestamoActivo, estado: 'DEVUELTO' });
    responderCargas([{ ...prestamoActivo, estado: 'DEVUELTO' }]);
    await render();
    expect(component.mensaje()).toContain('devuelto');
    expect(html.querySelectorAll('[data-cy=btn-devolver]').length).toBe(0);
  });

  it('RN-04 · muestra 404 cuando el préstamo no existe', async () => {
    component.devolver({ ...prestamoActivo, id: 999 });
    http.expectOne('/api/prestamos/999/devolucion').flush(
      { status: 404, mensaje: 'El préstamo no existe' },
      { status: 404, statusText: 'Not Found' },
    );
    await render();
    expect(component.mensaje()).toBe('El préstamo no existe');
  });

  it('RN-02 · registra un estudiante y lo deja seleccionado', async () => {
    component.dni = '70999999';
    component.nombre = 'Luis';
    component.registrarEstudiante();
    const req = http.expectOne((r) => r.method === 'POST' && r.url === '/api/estudiantes');
    req.flush({ id: 2, dni: '70999999', nombre: 'Luis', estado: 'ACTIVO' });
    responderCargas();
    await render();
    expect(component.mensaje()).toContain('registrado con estado ACTIVO');
    expect(component.estudianteId).toBe(2);
  });

  it('RN-02 · valida DNI y nombre obligatorios sin llamar al backend', () => {
    component.dni = ' ';
    component.nombre = 'Luis';
    component.registrarEstudiante();
    expect(component.exito()).toBe(false);
    http.expectNone((r) => r.method === 'POST');
  });

  it('sin conexión muestra un mensaje claro', async () => {
    component.devolver(prestamoActivo);
    http.expectOne('/api/prestamos/7/devolucion').error(new ProgressEvent('error'), { status: 0 });
    await render();
    expect(component.mensaje()).toBe('No hay conexión con el servidor.');
  });
});
