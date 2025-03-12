FROM nginx:alpine

# Instalar dependências
RUN apk add --no-cache \
    certbot \
    certbot-nginx \
    openssl \
    curl

# Criar diretório para certificados SSL
RUN mkdir -p /etc/nginx/ssl

# Copiar arquivos de configuração
COPY nginx.conf /etc/nginx/nginx.conf
COPY init-ssl.sh /docker-entrypoint.d/init-ssl.sh
RUN chmod +x /docker-entrypoint.d/init-ssl.sh

# Expor as portas 80 e 443
EXPOSE 80 443

# Comando para iniciar o Nginx
CMD ["nginx", "-g", "daemon off;"] 