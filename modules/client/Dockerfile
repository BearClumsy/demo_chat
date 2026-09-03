# Build context is modules/client:
#   docker build -t demo-chat-client modules/client

FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# Unprivileged nginx: runs as uid 101 and listens on 8080, so the container needs no root and
# satisfies a PodSecurity "restricted" namespace (see infra/k8s/manifest-*.yaml).
FROM nginxinc/nginx-unprivileged:alpine AS runtime
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
ENV BACKEND_URL=http://server:8080
EXPOSE 8080
