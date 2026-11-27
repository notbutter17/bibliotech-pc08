/// <reference types="cypress" />

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Cypress {
    interface Chainable {
      /** Crea un libro por API y devuelve el cuerpo creado. */
      crearLibro(ejemplares: number): Chainable<{ id: number; isbn: string; titulo: string }>;
      /** Crea un estudiante por API y devuelve el cuerpo creado. */
      crearEstudiante(): Chainable<{ id: number; dni: string; nombre: string }>;
    }
  }
}

const unico = () => `${Date.now()}${Math.floor(Math.random() * 1000)}`;

Cypress.Commands.add('crearLibro', (ejemplares: number) => {
  const id = unico();
  return cy
    .request('POST', '/api/libros', {
      isbn: `E2E-${id}`,
      titulo: `Libro E2E ${id}`,
      autor: 'Cypress',
      ejemplaresDisponibles: ejemplares,
    })
    .its('body');
});

Cypress.Commands.add('crearEstudiante', () => {
  const id = unico().slice(-8);
  return cy.request('POST', '/api/estudiantes', { dni: id, nombre: `Estudiante ${id}` }).its('body');
});

export {};
