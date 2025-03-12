FROM nginx:alpine

# Instalar dependências
RUN apk add --no-cache \
    certbot \
    certbot-nginx \
    openssl \
    curl \
    gettext

# Criar diretórios necessários
RUN mkdir -p /etc/nginx/ssl /etc/nginx/conf.d /var/www/certbot

# Configurar permissões
RUN chown -R nginx:nginx /etc/nginx/ssl \
    && chown -R nginx:nginx /etc/nginx/conf.d \
    && chown -R nginx:nginx /var/www/certbot \
    && chmod -R 755 /var/www/certbot

# Copiar arquivos de configuração
COPY nginx.conf /etc/nginx/nginx.conf
COPY init-ssl.sh /docker-entrypoint.d/init-ssl.sh
RUN chmod +x /docker-entrypoint.d/init-ssl.sh

# Configurar variável de ambiente padrão
ENV ENVIRONMENT=dev

# Expor as portas 80 e 443
EXPOSE 80 443

# Comando para iniciar o Nginx
CMD ["nginx", "-g", "daemon off;"] 