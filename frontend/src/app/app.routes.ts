import { Routes } from '@angular/router';
import { LibrosComponent } from './libros/libros.component';
import { PrestamosComponent } from './prestamos/prestamos.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'libros' },
  { path: 'libros', component: LibrosComponent, title: 'BiblioTech · Catálogo' },
  { path: 'prestamos', component: PrestamosComponent, title: 'BiblioTech · Préstamos' },
  { path: '**', redirectTo: 'libros' },
];
