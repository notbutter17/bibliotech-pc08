# syntax=docker/dockerfile:1
# ---------- Etapa 1: compilación de Angular ----------
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npx ng build --configuration production

# ---------- Etapa 2: servidor nginx ----------
FROM nginx:1.29-alpine
# La plantilla se procesa con envsubst al arrancar: BACKEND_URL y PORT vienen del entorno.
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/bibliotech-v2fe/browser/ /usr/share/nginx/html/
ENV BACKEND_URL=http://backend:8080 \
    PORT=80
EXPOSE 80
