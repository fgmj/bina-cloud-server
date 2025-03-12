#!/bin/sh

DOMAIN="bina.fernandojunior.com.br"
SSL_PATH="/etc/nginx/ssl/live/$DOMAIN"
CONF_PATH="/etc/nginx/conf.d"

# Copiar configuração inicial do Nginx
cp /etc/nginx/nginx.conf.template /etc/nginx/nginx.conf

if [ "$ENVIRONMENT" = "prod" ]; then
    echo "Ambiente de produção detectado"
    
    # Tentar obter certificado Let's Encrypt
    if [ ! -d "$SSL_PATH" ]; then
        echo "Gerando certificados Let's Encrypt para $DOMAIN..."
        mkdir -p /var/www/certbot
        
        # Primeiro tentar com --staging
        certbot certonly --webroot \
            --webroot-path /var/www/certbot \
            --email $CERTBOT_EMAIL \
            --agree-tos \
            --no-eff-email \
            -d $DOMAIN \
            --staging
        
        # Se o certificado de staging foi gerado com sucesso, tentar produção
        if [ -d "$SSL_PATH" ]; then
            rm -rf "$SSL_PATH"
            certbot certonly --webroot \
                --webroot-path /var/www/certbot \
                --email $CERTBOT_EMAIL \
                --agree-tos \
                --no-eff-email \
                -d $DOMAIN \
                --force-renewal
        fi
    fi
    
    # Verificar se os certificados foram obtidos com sucesso
    if [ -f "$SSL_PATH/fullchain.pem" ] && [ -f "$SSL_PATH/privkey.pem" ]; then
        echo "Certificados Let's Encrypt obtidos com sucesso"
        cp /etc/nginx/ssl.conf.template "$CONF_PATH/ssl.conf"
        nginx -s reload
    else
        echo "Falha ao obter certificados Let's Encrypt"
    fi
    
    # Configurar renovação automática
    echo "0 0,12 * * * root certbot renew --quiet" > /etc/crontabs/root
    crond
else
    echo "Ambiente de desenvolvimento detectado"
    
    # Gerar certificados auto-assinados
    if [ ! -d "$SSL_PATH" ]; then
        echo "Gerando certificados auto-assinados para desenvolvimento..."
        mkdir -p "$SSL_PATH"
        
        openssl req -x509 -nodes -newkey rsa:2048 -days 365 \
            -keyout "$SSL_PATH/privkey.pem" \
            -out "$SSL_PATH/fullchain.pem" \
            -subj "/CN=$DOMAIN"
        
        cp "$SSL_PATH/fullchain.pem" "$SSL_PATH/chain.pem"
        chmod 644 "$SSL_PATH"/*.pem
    fi
    
    # Copiar configuração SSL
    cp /etc/nginx/ssl.conf.template "$CONF_PATH/ssl.conf"
fi

# Ajustar permissões
chown -R nginx:nginx /etc/nginx/ssl
chmod -R 644 /etc/nginx/ssl
find /etc/nginx/ssl -type d -exec chmod 755 {} \;

# Iniciar Nginx
exec nginx -g 'daemon off;' 