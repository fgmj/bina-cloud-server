FROM nginx:alpine

# Instalar dependências para utilitários e fuso horário
RUN apk add --no-cache \
    curl \
    tzdata \
    openssl \
    bash

# Definir timezone
ENV TZ=America/Sao_Paulo

# Criar diretórios necessários
RUN mkdir -p /etc/nginx/conf.d \
    /var/www/certbot \
    /etc/letsencrypt/live \
    /etc/letsencrypt/archive \
    /etc/letsencrypt/renewal

# Gerar dhparam e copiar configurações SSL
RUN openssl dhparam -out /etc/letsencrypt/ssl-dhparams.pem 2048
COPY options-ssl-nginx.conf /etc/letsencrypt/options-ssl-nginx.conf

# Ajustar permissões
RUN chown -R nginx:nginx /etc/nginx /var/www/certbot /etc/letsencrypt

# Copiar nginx.conf diretamente
COPY nginx.conf /etc/nginx/nginx.conf

# Expor portas HTTP e HTTPS
EXPOSE 80 443

# Comando padrão do container
CMD ["nginx", "-g", "daemon off;"]
