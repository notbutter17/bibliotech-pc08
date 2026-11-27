import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Libro } from '../shared/modelos';
import { mensajeDeError } from '../shared/mensaje-error';
import { LibroService } from './libro.service';

/** Pantalla 1 · Catálogo de libros (RN-01). */
@Component({
  selector: 'app-libros',
  imports: [ReactiveFormsModule],
  templateUrl: './libros.component.html',
  styleUrl: './libros.component.css',
})
export class LibrosComponent implements OnInit {
  private readonly libroService = inject(LibroService);
  private readonly fb = inject(FormBuilder);

  readonly libros = signal<Libro[]>([]);
  readonly mensaje = signal('');
  readonly error = signal('');
  readonly enviado = signal(false);

  readonly form = this.fb.nonNullable.group({
    isbn: ['', [Validators.required, Validators.pattern(/\S/)]],
    titulo: ['', [Validators.required, Validators.pattern(/\S/)]],
    autor: ['', [Validators.required, Validators.pattern(/\S/)]],
    ejemplaresDisponibles: [1, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.libroService.listar().subscribe({
      next: (libros) => this.libros.set(libros),
      error: (err) => this.error.set(mensajeDeError(err, 'No se pudo cargar el catálogo.')),
    });
  }

  /** true si el control debe mostrar el error indicado. */
  invalido(campo: keyof typeof this.form.controls, error: string): boolean {
    const control = this.form.controls[campo];
    return control.hasError(error) && (control.touched || this.enviado());
  }

  registrar(): void {
    this.enviado.set(true);
    this.mensaje.set('');
    this.error.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.libroService.registrar(this.form.getRawValue()).subscribe({
      next: (libro) => {
        this.mensaje.set(`Libro "${libro.titulo}" registrado correctamente.`);
        this.form.reset();
        this.enviado.set(false);
        this.cargar();
      },
      error: (err) => this.error.set(mensajeDeError(err, 'No se pudo registrar el libro.')),
    });
  }
}
