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

# Ajustar permissões
RUN chown -R nginx:nginx /etc/nginx /var/www/certbot /etc/letsencrypt

# Copiar nginx.conf diretamente
COPY nginx.conf /etc/nginx/nginx.conf

# Expor portas HTTP e HTTPS
EXPOSE 80 443

# Comando padrão do container
CMD ["nginx", "-g", "daemon off;"]
