import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LibrosComponent } from './libros.component';
import { Libro } from '../shared/modelos';

describe('LibrosComponent (Pantalla 1)', () => {
  let fixture: ComponentFixture<LibrosComponent>;
  let component: LibrosComponent;
  let http: HttpTestingController;
  let html: HTMLElement;

  const catalogo: Libro[] = [
    { id: 1, isbn: '978-1', titulo: 'Clean Code', autor: 'R. Martin', ejemplaresDisponibles: 2, estado: 'DISPONIBLE' },
    { id: 2, isbn: '978-2', titulo: 'Refactoring', autor: 'M. Fowler', ejemplaresDisponibles: 0, estado: 'NO DISPONIBLE' },
  ];

  async function render(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
  }

  function escribir(selector: string, valor: string): void {
    const input = html.querySelector(`[data-cy=${selector}]`) as HTMLInputElement;
    input.value = valor;
    input.dispatchEvent(new Event('input'));
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LibrosComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    fixture = TestBed.createComponent(LibrosComponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    html = fixture.nativeElement;
    fixture.detectChanges();
    http.expectOne('/api/libros').flush(catalogo);
    await render();
  });

  afterEach(() => http.verify());

  it('lista los libros del backend con su estado', () => {
    const filas = html.querySelectorAll('[data-cy=fila-libro]');
    expect(filas.length).toBe(2);
    expect(filas[0].textContent).toContain('Clean Code');
    expect(filas[1].textContent).toContain('NO DISPONIBLE');
  });

  it('muestra "ISBN obligatorio" y no llama al backend si falta el ISBN', async () => {
    // Arrange
    escribir('titulo', 'TDD');
    escribir('autor', 'Kent Beck');
    // Act
    component.registrar();
    await render();
    // Assert
    expect(html.querySelector('[data-cy=error-isbn]')?.textContent).toContain('El ISBN es obligatorio');
    http.expectNone('/api/libros');
  });

  it('muestra error si los ejemplares son negativos', async () => {
    // Arrange
    escribir('isbn', '978-3');
    escribir('titulo', 'TDD');
    escribir('autor', 'Kent Beck');
    escribir('ejemplares', '-1');
    // Act
    component.registrar();
    await render();
    // Assert
    expect(html.querySelector('[data-cy=error-ejemplares]')?.textContent).toContain('no pueden ser negativos');
    expect(component.form.invalid).toBe(true);
  });

  it('muestra errores de título y autor vacíos', async () => {
    escribir('isbn', '978-3');
    escribir('titulo', '   ');
    component.registrar();
    await render();
    expect(html.querySelector('[data-cy=error-titulo]')).not.toBeNull();
    expect(html.querySelector('[data-cy=error-autor]')).not.toBeNull();
  });

  it('con ejemplares = 0 (borde) el formulario es válido', () => {
    component.form.setValue({ isbn: '978-3', titulo: 'TDD', autor: 'Kent Beck', ejemplaresDisponibles: 0 });
    expect(component.form.valid).toBe(true);
  });

  it('registra un libro válido (POST) y recarga la lista', async () => {
    // Arrange
    component.form.setValue({ isbn: '978-3', titulo: 'TDD', autor: 'Kent Beck', ejemplaresDisponibles: 1 });
    // Act
    component.registrar();
    const post = http.expectOne((r) => r.method === 'POST' && r.url === '/api/libros');
    post.flush({ id: 3, isbn: '978-3', titulo: 'TDD', autor: 'Kent Beck', ejemplaresDisponibles: 1, estado: 'DISPONIBLE' });
    http.expectOne((r) => r.method === 'GET').flush([...catalogo, post.request.body]);
    await render();
    // Assert
    expect(post.request.body.isbn).toBe('978-3');
    expect(html.querySelector('[data-cy=mensaje]')?.textContent).toContain('registrado correctamente');
    expect(html.querySelectorAll('[data-cy=fila-libro]').length).toBe(3);
  });

  it('muestra el mensaje del backend cuando el ISBN está repetido (409)', async () => {
    component.form.setValue({ isbn: '978-1', titulo: 'Otro', autor: 'X', ejemplaresDisponibles: 1 });
    component.registrar();
    http.expectOne((r) => r.method === 'POST').flush(
      { status: 409, mensaje: 'Ya existe un libro con el ISBN 978-1' },
      { status: 409, statusText: 'Conflict' },
    );
    await render();
    expect(html.querySelector('[data-cy=error]')?.textContent).toContain('Ya existe un libro con el ISBN 978-1');
  });
});
