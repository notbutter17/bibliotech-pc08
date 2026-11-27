import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LibroService } from '../libros/libro.service';
import { Estudiante, Libro, Prestamo } from '../shared/modelos';
import { mensajeDeError } from '../shared/mensaje-error';
import { EstudianteService } from './estudiante.service';
import { PrestamoService } from './prestamo.service';

/** Pantalla 2 · Registrar préstamo y devolver (RN-02, RN-03, RN-04). */
@Component({
  selector: 'app-prestamos',
  imports: [FormsModule],
  templateUrl: './prestamos.component.html',
  styleUrl: './prestamos.component.css',
})
export class PrestamosComponent implements OnInit {
  private readonly prestamoService = inject(PrestamoService);
  private readonly libroService = inject(LibroService);
  private readonly estudianteService = inject(EstudianteService);

  readonly libros = signal<Libro[]>([]);
  readonly estudiantes = signal<Estudiante[]>([]);
  readonly prestamos = signal<Prestamo[]>([]);

  /** Mensaje visible y si corresponde a éxito o error. */
  readonly mensaje = signal('');
  readonly exito = signal(true);

  estudianteId: number | null = null;
  libroId: number | null = null;
  dni = '';
  nombre = '';

  ngOnInit(): void {
    this.cargarTodo();
  }

  cargarTodo(): void {
    this.libroService.listar().subscribe({ next: (l) => this.libros.set(l), error: (e) => this.fallo(e) });
    this.estudianteService.listar().subscribe({ next: (e) => this.estudiantes.set(e), error: (e) => this.fallo(e) });
    this.prestamoService.listar().subscribe({ next: (p) => this.prestamos.set(p), error: (e) => this.fallo(e) });
  }

  registrarEstudiante(): void {
    if (!this.dni.trim() || !this.nombre.trim()) {
      this.aviso(false, 'El DNI y el nombre son obligatorios.');
      return;
    }
    this.estudianteService.registrar({ dni: this.dni.trim(), nombre: this.nombre.trim() }).subscribe({
      next: (e) => {
        this.aviso(true, `Estudiante ${e.nombre} registrado con estado ${e.estado}.`);
        this.dni = '';
        this.nombre = '';
        this.estudianteId = e.id ?? null;
        this.cargarTodo();
      },
      error: (err) => this.fallo(err),
    });
  }

  registrarPrestamo(): void {
    if (this.estudianteId == null || this.libroId == null) {
      this.aviso(false, 'Selecciona un estudiante y un libro.');
      return;
    }
    this.prestamoService.registrar(Number(this.estudianteId), Number(this.libroId)).subscribe({
      next: (p) => {
        this.aviso(true, `Préstamo #${p.id} registrado. Quedan ${p.libro.ejemplaresDisponibles} ejemplares de "${p.libro.titulo}".`);
        this.cargarTodo();
      },
      error: (err) => this.fallo(err),
    });
  }

  devolver(prestamo: Prestamo): void {
    this.prestamoService.devolver(prestamo.id).subscribe({
      next: (p) => {
        this.aviso(true, `Préstamo #${p.id} devuelto. El libro "${p.libro.titulo}" recuperó un ejemplar.`);
        this.cargarTodo();
      },
      error: (err) => this.fallo(err),
    });
  }

  private aviso(exito: boolean, texto: string): void {
    this.exito.set(exito);
    this.mensaje.set(texto);
  }

  private fallo(err: unknown): void {
    this.aviso(false, mensajeDeError(err));
  }
}
