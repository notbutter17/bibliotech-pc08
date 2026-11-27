import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('muestra el título y la navegación a las dos pantallas', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const html = fixture.nativeElement as HTMLElement;
    expect(html.querySelector('h1')?.textContent).toContain('BiblioTech');
    expect(html.querySelector('[data-cy=nav-libros]')?.getAttribute('href')).toBe('/libros');
    expect(html.querySelector('[data-cy=nav-prestamos]')?.getAttribute('href')).toBe('/prestamos');
  });
});
