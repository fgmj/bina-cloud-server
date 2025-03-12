FROM nginx:alpine

# Instalar dependências
RUN apk add --no-cache \
    certbot \
    certbot-nginx \
    openssl \
    curl \
    gettext

# Criar diretórios necessários
RUN mkdir -p /etc/nginx/ssl /etc/nginx/conf.d

# Configurar usuário nginx
RUN adduser -D -H -u 101 -s /sbin/nologin nginx \
    && chown -R nginx:nginx /etc/nginx/ssl

# Copiar arquivos de configuração
COPY default.conf /etc/nginx/nginx.conf
COPY nginx.conf /etc/nginx/conf.d/app.conf
COPY init-ssl.sh /docker-entrypoint.d/init-ssl.sh
RUN chmod +x /docker-entrypoint.d/init-ssl.sh

# Configurar permissões
RUN chown -R nginx:nginx /etc/nginx/conf.d \
    && chmod -R 644 /etc/nginx/conf.d/* \
    && chmod 755 /etc/nginx/conf.d

# Configurar variável de ambiente padrão
ENV NGINX_HOST=localhost

# Expor as portas 80 e 443
EXPOSE 80 443

# Usar usuário nginx
USER nginx

# Comando para iniciar o Nginx
CMD ["nginx", "-g", "daemon off;"] 