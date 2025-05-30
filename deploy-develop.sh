#!/bin/bash

# Definir diretório do projeto
PROJECT_DIR="/home/ubuntu/bina-cloud-server-develop"

# Criar diretório se não existir
mkdir -p "$PROJECT_DIR"

# Acessar o diretório do projeto
cd "$PROJECT_DIR" || { echo "Erro: Diretório não encontrado!"; exit 1; }

# Parar containers específicos do ambiente de desenvolvimento
echo "Parando containers do ambiente de desenvolvimento..."
docker stop bina-cloud-server-develop-app bina-cloud-server-develop-nginx || true
docker rm bina-cloud-server-develop-app bina-cloud-server-develop-nginx || true

# Atualizar repositório
echo "Atualizando repositório via git pull..."
git fetch origin
git reset --hard origin/develop
git pull origin develop

# Construir e iniciar os containers com portas diferentes
echo "Subindo containers com Docker Compose..."
ENV=DEV \
NGINX_HOST=dev.bina.fernandojunior.com.br \
APP_PORT=8081 \
NGINX_PORT=8082 \
COMPOSE_PROJECT_NAME=bina-cloud-server-develop \
docker compose up -d --build

echo "Deploy do ambiente de desenvolvimento concluído!" 