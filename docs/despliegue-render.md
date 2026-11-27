# Despliegue en Render (HTTPS) · pasos únicos de configuración

1. **Imágenes públicas**: tras la primera ejecución de `cd.yml`, en GitHub → *Packages* → `bibliotech-backend` y `bibliotech-frontend` → *Package settings* → *Change visibility* → **Public**.
2. **Blueprint**: en https://dashboard.render.com → *New* → *Blueprint* → conectar `notbutter17/bibliotech-pc08` → *Apply*. Se crean `bibliotech-db` (PostgreSQL), `bibliotech-backend` y `bibliotech-frontend`. Las credenciales de la base las genera Render y se inyectan como variables `DB_*` (no están en el repositorio).
3. Si Render asignó otra URL al backend, actualiza `BACKEND_URL` en el servicio `bibliotech-frontend`.
4. **Deploy hooks**: en cada servicio → *Settings* → *Deploy Hook* → copiar la URL y guardarla como secreto de GitHub:
   ```bash
   gh secret set RENDER_DEPLOY_HOOK_BACKEND
   gh secret set RENDER_DEPLOY_HOOK_FRONTEND
   gh variable set FRONTEND_URL --body "https://bibliotech-frontend.onrender.com"
   ```
5. **SonarCloud**: https://sonarcloud.io → *Log in with GitHub* → importar `notbutter17/bibliotech-pc08` → *Administration → Analysis Method* → desactivar *Automatic Analysis* → *My Account → Security* → generar token → `gh secret set SONAR_TOKEN`.

Secretos usados: `SONAR_TOKEN`, `RENDER_DEPLOY_HOOK_BACKEND`, `RENDER_DEPLOY_HOOK_FRONTEND` (GitHub Secrets) y `DB_PASSWORD` (generado por Render). `GITHUB_TOKEN` lo provee Actions para publicar en GHCR.
