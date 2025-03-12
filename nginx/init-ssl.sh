#!/bin/sh

# Verificar se o domínio foi fornecido
if [ -z "$DOMAIN" ]; then
    echo "DOMAIN não foi definido. Usando localhost..."
    DOMAIN="localhost"
fi

# Verificar se os certificados já existem
if [ ! -f "/etc/nginx/ssl/live/$DOMAIN/fullchain.pem" ]; then
    echo "Certificados SSL não encontrados. Gerando certificados auto-assinados para desenvolvimento..."
    
    # Criar diretório para certificados
    mkdir -p "/etc/nginx/ssl/live/$DOMAIN"
    
    # Gerar certificados auto-assinados
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout "/etc/nginx/ssl/live/$DOMAIN/privkey.pem" \
        -out "/etc/nginx/ssl/live/$DOMAIN/fullchain.pem" \
        -subj "/CN=$DOMAIN"
else
    echo "Certificados SSL encontrados."
fi

# Se estiver em produção e tiver email configurado, tentar renovar certificados
if [ "$ENVIRONMENT" = "prod" ] && [ ! -z "$CERTBOT_EMAIL" ]; then
    echo "Tentando renovar certificados SSL..."
    certbot renew --nginx --non-interactive --agree-tos --email "$CERTBOT_EMAIL"
fi

# Iniciar o Nginx
exec nginx -g 'daemon off;' 