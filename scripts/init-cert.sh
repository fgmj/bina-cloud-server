#!/bin/bash

# Domínio e email
DOMAIN="bina.fernandojunior.com.br"
EMAIL="fernando.medeiros@gmail.com" # <-- Altere para seu e-mail real

# Caminhos dos volumes usados no docker-compose
CERTS_VOLUME="ssl-certs"
WEBROOT_VOLUME="certbot-web"

echo "🔐 Iniciando emissão inicial do certificado Let's Encrypt para: $DOMAIN"
echo "📁 Montando volumes: $CERTS_VOLUME e $WEBROOT_VOLUME"
echo

docker run --rm \
  -v "${CERTS_VOLUME}:/etc/letsencrypt" \
  -v "${WEBROOT_VOLUME}:/var/www/certbot" \
  certbot/certbot certonly \
  --webroot \
  --webroot-path=/var/www/certbot \
  --email "$EMAIL" \
  --agree-tos \
  --no-eff-email \
  -d "$DOMAIN"

RESULT=$?

if [ $RESULT -eq 0 ]; then
  echo
  echo "✅ Certificado emitido com sucesso!"
  echo "🔁 Agora você pode subir o nginx normalmente com 'docker-compose up -d'"
else
  echo
  echo "❌ Falha ao emitir o certificado. Verifique o domínio, DNS e se as portas 80/443 estão liberadas."
fi
