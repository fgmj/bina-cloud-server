#!/bin/bash

# Definir diretório do projeto
PROJECT_DIR="/home/ubuntu/bina-cloud-server"

# Acessar o diretório do projeto
cd "$PROJECT_DIR" || { echo "Erro: Diretório não encontrado!"; exit 1; }

# Parar containers específicos do ambiente de produção
echo "Parando containers do ambiente de produção..."
docker stop bina-cloud-server-app bina-cloud-server-nginx || true
docker rm bina-cloud-server-app bina-cloud-server-nginx || true

# Atualizar repositório
echo "Atualizando repositório via git pull..."
git fetch origin
git reset --hard origin/main
git pull origin main

# Construir e iniciar os containers
echo "Subindo containers com Docker Compose..."
ENV=PROD \
NGINX_HOST=bina.fernandojunior.com.br \
APP_PORT=8080 \
NGINX_PORT=80 \
COMPOSE_PROJECT_NAME=bina-cloud-server \
docker compose up -d --build

echo "Deploy do ambiente de produção concluído!"

#echo "Mostrando logs do serviço 'app'..."
#docker compose logs -f app