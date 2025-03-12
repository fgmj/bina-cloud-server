#!/bin/sh

DOMAIN="bina.fernandojunior.com.br"
SSL_PATH="/etc/nginx/ssl/live/$DOMAIN"

if [ "$ENVIRONMENT" = "prod" ]; then
    # Produção: Usar Let's Encrypt
    if [ ! -d "$SSL_PATH" ]; then
        echo "Gerando certificados Let's Encrypt para $DOMAIN..."
        mkdir -p /var/www/certbot
        certbot certonly --webroot \
            --webroot-path /var/www/certbot \
            --email $CERTBOT_EMAIL \
            --agree-tos \
            --no-eff-email \
            -d $DOMAIN \
            --staging
        
        # Se o certificado foi gerado com sucesso, remover --staging
        if [ -d "$SSL_PATH" ]; then
            certbot certonly --webroot \
                --webroot-path /var/www/certbot \
                --email $CERTBOT_EMAIL \
                --agree-tos \
                --no-eff-email \
                -d $DOMAIN \
                --force-renewal
        fi
    else
        echo "Certificados Let's Encrypt já existem. Tentando renovar..."
        certbot renew
    fi
else
    # Desenvolvimento: Gerar certificados auto-assinados
    if [ ! -d "$SSL_PATH" ]; then
        echo "Gerando certificados auto-assinados para desenvolvimento..."
        mkdir -p "$SSL_PATH"
        
        # Gerar chave privada e CSR
        openssl req -x509 -nodes -newkey rsa:2048 -days 365 \
            -keyout "$SSL_PATH/privkey.pem" \
            -out "$SSL_PATH/fullchain.pem" \
            -subj "/CN=$DOMAIN"
        
        # Copiar fullchain.pem para chain.pem
        cp "$SSL_PATH/fullchain.pem" "$SSL_PATH/chain.pem"
        
        # Ajustar permissões
        chmod 644 "$SSL_PATH"/*.pem
    else
        echo "Certificados auto-assinados já existem."
    fi
fi

# Iniciar Nginx
exec nginx -g 'daemon off;' 