describe('Flujo 3 · Prestar un libro y ver bajar sus ejemplares (RN-03 / RN-04)', () => {
  it('al prestar baja un ejemplar y al devolver se restituye', () => {
    cy.crearEstudiante().then((estudiante) => {
      cy.crearLibro(2).then((libro) => {
        cy.visit('/prestamos');
        const fila = () => cy.get(`[data-cy=fila-existencia][data-isbn="${libro.isbn}"]`);
        fila().find('[data-cy=ejemplares]').should('have.text', '2');

        cy.get('[data-cy=select-estudiante]').select(`${estudiante.dni} · ${estudiante.nombre} (ACTIVO)`);
        cy.get('[data-cy=select-libro]').select(`${libro.titulo} (2 ejemplares)`);
        cy.get('[data-cy=btn-prestar]').click();

        cy.get('[data-cy=mensaje]').should('have.class', 'ok').and('contain', 'Quedan 1 ejemplares');
        fila().find('[data-cy=ejemplares]').should('have.text', '1');
        cy.screenshot('flujo3-ejemplar-baja');

        cy.contains('[data-cy=fila-prestamo]', libro.titulo).find('[data-cy=btn-devolver]').click();
        cy.get('[data-cy=mensaje]').should('contain', 'devuelto');
        fila().find('[data-cy=ejemplares]').should('have.text', '2');
        cy.contains('[data-cy=fila-prestamo]', libro.titulo).should('contain', 'DEVUELTO');
        cy.screenshot('flujo3-devolucion');
      });
    });
  });

  it('rechaza el préstamo de un libro sin ejemplares (NO DISPONIBLE)', () => {
    cy.crearEstudiante().then((estudiante) => {
      cy.crearLibro(0).then((libro) => {
        cy.visit('/prestamos');
        cy.get('[data-cy=select-estudiante]').select(`${estudiante.dni} · ${estudiante.nombre} (ACTIVO)`);
        cy.get('[data-cy=select-libro]').select(`${libro.titulo} (0 ejemplares)`);
        cy.get('[data-cy=btn-prestar]').click();
        cy.get('[data-cy=mensaje]').should('have.class', 'ko').and('contain', 'El libro no tiene ejemplares disponibles');
        cy.screenshot('flujo3-sin-ejemplares');
      });
    });
  });
});
