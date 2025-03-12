#!/bin/sh

# Definir host padrão se não estiver definido
NGINX_HOST=${NGINX_HOST:-localhost}

# Criar diretório para certificados
mkdir -p /etc/nginx/ssl/live/$NGINX_HOST

# Verificar se os certificados já existem
if [ ! -f "/etc/nginx/ssl/live/$NGINX_HOST/fullchain.pem" ]; then
    echo "Certificados SSL não encontrados. Gerando certificados auto-assinados para desenvolvimento..."
    
    # Gerar chave privada e certificado auto-assinado
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout /etc/nginx/ssl/live/$NGINX_HOST/privkey.pem \
        -out /etc/nginx/ssl/live/$NGINX_HOST/fullchain.pem \
        -subj "/C=BR/ST=SP/L=Sao Paulo/O=Bina Cloud/CN=$NGINX_HOST"
fi

# Se estiver em produção e tiver email configurado, tentar Let's Encrypt
if [ "$ENVIRONMENT" = "prod" ] && [ ! -z "$CERTBOT_EMAIL" ]; then
    echo "Tentando renovar certificados SSL..."
    certbot --nginx \
        --non-interactive \
        --agree-tos \
        --email $CERTBOT_EMAIL \
        --domains $NGINX_HOST \
        --keep-until-expiring
fi

# Iniciar Nginx
exec nginx -g 'daemon off;' 