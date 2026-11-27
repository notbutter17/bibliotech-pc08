describe('Flujo 2 · Registrar un estudiante (RN-02)', () => {
  it('registra un estudiante con estado ACTIVO y queda disponible para prestar', () => {
    const dni = `${Date.now()}`.slice(-8);
    cy.visit('/prestamos');
    cy.get('[data-cy=dni]').type(dni);
    cy.get('[data-cy=nombre]').type('Lucía Ramos');
    cy.get('[data-cy=btn-registrar-estudiante]').click();

    cy.get('[data-cy=mensaje]')
      .should('have.class', 'ok')
      .and('contain', 'Lucía Ramos registrado con estado ACTIVO');
    cy.get('[data-cy=select-estudiante] option').should('contain', `${dni} · Lucía Ramos (ACTIVO)`);
    cy.screenshot('flujo2-estudiante-registrado');
  });

  it('rechaza un DNI repetido con el mensaje del backend', () => {
    cy.crearEstudiante().then((e) => {
      cy.visit('/prestamos');
      cy.get('[data-cy=dni]').type(e.dni);
      cy.get('[data-cy=nombre]').type('Duplicado');
      cy.get('[data-cy=btn-registrar-estudiante]').click();
      cy.get('[data-cy=mensaje]').should('have.class', 'ko').and('contain', `Ya existe un estudiante con el DNI ${e.dni}`);
    });
  });
});
