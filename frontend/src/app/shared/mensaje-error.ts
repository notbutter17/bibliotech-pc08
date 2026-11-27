import { HttpErrorResponse } from '@angular/common/http';

/** Extrae el mensaje de negocio del backend; si no hay, devuelve un texto genérico. */
export function mensajeDeError(err: unknown, porDefecto = 'No se pudo completar la operación.'): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'No hay conexión con el servidor.';
    }
    const mensaje = (err.error as { mensaje?: string } | null)?.mensaje;
    if (mensaje) {
      return mensaje;
    }
  }
  return porDefecto;
}
