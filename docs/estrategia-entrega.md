# Estrategia de entrega · BiblioTech

## Decisión: **blue-green** (con la variante nativa de Render)

| Estrategia | Para BiblioTech | Veredicto |
|---|---|---|
| Rolling | Útil con muchas réplicas; BiblioTech corre **una sola instancia** por servicio, así que «rolling» degenera en reemplazar la única instancia. | No aporta |
| Canary | Requiere dividir tráfico por porcentaje y métricas para decidir. Con unos pocos usuarios (una biblioteca universitaria) no hay tráfico suficiente para que un 5 % sea representativo. | Excesivo |
| **Blue-green** | Dos versiones completas: la nueva (green) arranca y se verifica **antes** de recibir tráfico; la anterior (blue) sigue disponible para volver atrás al instante. | **Elegida** |

### Cómo se aplica
1. `cd.yml` publica `bibliotech-backend:X.Y.Z` y `bibliotech-frontend:X.Y.Z` en GHCR (inmutables) y llama a los deploy hooks de Render con `imgURL=...:X.Y.Z`.
2. Render levanta la nueva instancia (green) en paralelo y **solo cambia el tráfico cuando pasa el health check** (`healthCheckPath: /api/libros`). Si no arranca, la versión anterior (blue) sigue sirviendo: cero downtime.
3. El job termina con una **prueba de humo** sobre la URL pública HTTPS (`/api/libros` debe responder 200).

### Rollback
- **Automático**: si la versión nueva no pasa el health check, Render no cambia el tráfico.
- **Manual (1 minuto)**: Render → servicio → *Deploys* → *Rollback* sobre el despliegue anterior; o volver a ejecutar el hook con la imagen previa:
  `curl -X POST "$RENDER_DEPLOY_HOOK_BACKEND&imgURL=ghcr.io/notbutter17/bibliotech-backend:1.0.0"`
- **Por Git**: `git revert` del merge en `develop` → PR a `main` → `cd.yml` publica `X.Y.(Z+1)` y despliega.
- **Base de datos**: los cambios de esquema son solo aditivos (columnas nuevas con valor por defecto), de modo que la versión blue sigue funcionando con el esquema de la green.
