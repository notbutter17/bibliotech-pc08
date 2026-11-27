# Plan de rendimiento · JMeter 5.6.3

Plan: `bibliotech-plan.jmx`

| Grupo | Petición | Usuarios (por defecto) | Iteraciones | Aserciones |
|---|---|---|---|---|
| setUp | `POST /api/libros` (libro con 100 000 ejemplares) | 1 | 1 | 201 |
| Catálogo | `GET /api/libros` | 50 (`-JusuariosGet`) | 20 | 200 y ≤ 500 ms |
| Préstamos | `POST /api/estudiantes` + `POST /api/prestamos` | 25 (`-JusuariosPost`) | 8 | 201 y ≤ 500 ms |
| tearDown | `GET /api/libros/{id}` | 1 | 1 | **stock final = 100 000 − préstamos OK** |

Ejecutar (con `docker compose up -d` levantado; puerto 4200 = entrada real por nginx):

```bash
jmeter -n -t performance/bibliotech-plan.jmx -Jport=4200 -l resultados/r.jtl -e -o resultados/r
# estrés: -JusuariosGet=100 -JusuariosPost=100 -Jrampa=1 -JiterGet=20 -JiterPost=10
# contra Render: -Jproto=https -Jhost=bibliotech-frontend.onrender.com -Jport=443
```

## Resultados (Docker Compose local, MacBook, 27/11/2026)

| Escenario | Endpoint | Peticiones | Prom. (ms) | p90 | p95 | Máx | Error | Rendimiento (req/s) |
|---|---|---|---|---|---|---|---|---|
| Nominal (50 + 25 usuarios, rampa 10 s) | GET /api/libros | 1000 | 2,7 | 5 | 6 | 83 | 0 % | 99,5 |
| | POST /api/prestamos | 200 | 5,0 | 8 | 9 | 85 | 0 % | 19,9 |
| Estrés (100 + 100 usuarios, rampa 1 s) | GET /api/libros | 2000 | 22,5 | 53 | 67 | 145 | 0 % | 1270,6 |
| | POST /api/prestamos | 1000 | 25,8 | 58 | 70 | 141 | 0 % | 635,3 |
| | **Integridad de stock** | 1000 préstamos | — | — | — | — | **FALLA: stock 99 633, esperado 99 000** | — |
| Estrés tras corrección | GET /api/libros | 2000 | 72,2 | 153 | 182 | 382 | 0 % | 727,3 |
| | POST /api/prestamos | 1000 | 81,1 | 173 | 193 | 388 | 0 % | 363,6 |
| | **Integridad de stock** | 1000 préstamos | — | — | — | — | **OK: 99 000** | — |
| Nominal tras corrección | GET /api/libros | 1000 | 1,7 | 3 | 4 | 55 | 0 % | 98,9 |
| | POST /api/prestamos | 200 | 3,3 | 5 | 7 | 15 | 0 % | 19,8 |

**Hallazgo:** bajo estrés, 1000 préstamos descontaron solo 367 ejemplares (*lost update*: dos transacciones leen el mismo stock y ambas escriben stock − 1).
**Acción:** `LibroRepository.findByIdParaActualizar` con `@Lock(PESSIMISTIC_WRITE)` (SELECT … FOR UPDATE) en `registrarPrestamo` y `devolverLibro`, más la prueba de regresión `prestamosConcurrentes_noPierdenActualizaciones` (falla sin el bloqueo: «expected 30 but was 47»).
**Costo:** con 100 usuarios simultáneos la latencia sube (p95 70 → 193 ms) porque los préstamos del mismo libro se serializan; con carga nominal no hay diferencia (p95 9 → 7 ms) y todo queda bajo el SLA de 500 ms.

Reportes HTML: `resultados/<escenario>/index.html`.
