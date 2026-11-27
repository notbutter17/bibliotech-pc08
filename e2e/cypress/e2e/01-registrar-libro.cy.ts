describe('Flujo 1 · Registrar un libro y verlo en la lista (RN-01)', () => {
  beforeEach(() => cy.visit('/libros'));

  it('registra un libro nuevo y aparece en el catálogo como DISPONIBLE', () => {
    const isbn = `978-${Date.now()}`;
    cy.get('[data-cy=isbn]').type(isbn);
    cy.get('[data-cy=titulo]').type('Pruebas de Software');
    cy.get('[data-cy=autor]').type('Equipo QA');
    cy.get('[data-cy=ejemplares]').clear().type('4');
    cy.get('[data-cy=btn-registrar-libro]').click();

    cy.get('[data-cy=mensaje]').should('contain', 'registrado correctamente');
    cy.contains('[data-cy=fila-libro]', isbn)
      .should('contain', 'Pruebas de Software')
      .and('contain', 'DISPONIBLE')
      .find('[data-cy=ejemplares-libro]')
      .should('have.text', '4');
    cy.screenshot('flujo1-libro-registrado');
  });

  it('muestra los errores de validación si falta el ISBN o los ejemplares son negativos', () => {
    cy.get('[data-cy=titulo]').type('Sin ISBN');
    cy.get('[data-cy=autor]').type('Anónimo');
    cy.get('[data-cy=ejemplares]').clear().type('-2');
    cy.get('[data-cy=btn-registrar-libro]').click();

    cy.get('[data-cy=error-isbn]').should('contain', 'El ISBN es obligatorio');
    cy.get('[data-cy=error-ejemplares]').should('contain', 'no pueden ser negativos');
    cy.screenshot('flujo1-validaciones');
  });
});
