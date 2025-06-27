#!/bin/bash

# Script simplificado para testar emissão de certificados
DOMAIN="bina.fernandojunior.com.br"
EMAIL="fernando.medeiros@gmail.com"

echo "🔐 Teste de Emissão de Certificados"
echo "===================================="
echo "Domínio: $DOMAIN"
echo "Email: $EMAIL"
echo

# Verificar se o domínio resolve
echo "🔍 Verificando DNS..."
if nslookup "$DOMAIN" > /dev/null 2>&1; then
    echo "✅ DNS OK"
else
    echo "❌ DNS não resolve"
    exit 1
fi

# Verificar se as portas estão livres
echo "🔍 Verificando portas..."
if ss -tuln | grep -q ":80 "; then
    echo "⚠️ Porta 80 em uso"
else
    echo "✅ Porta 80 livre"
fi

if ss -tuln | grep -q ":443 "; then
    echo "⚠️ Porta 443 em uso"
else
    echo "✅ Porta 443 livre"
fi

# Criar volumes se não existirem
echo "🔧 Criando volumes..."
docker volume create ssl-certs 2>/dev/null || echo "Volume ssl-certs já existe"
docker volume create certbot-web 2>/dev/null || echo "Volume certbot-web já existe"

# Criar nginx simples para validação
echo "🚀 Criando nginx temporário..."
cat > nginx-temp.conf << 'EOF'
events {
    worker_connections 1024;
}

http {
    server {
        listen 80;
        server_name bina.fernandojunior.com.br;
        
        location /.well-known/acme-challenge/ {
            root /var/www/certbot;
            try_files $uri =404;
        }
        
        location / {
            return 200 "Nginx OK";
        }
    }
}
EOF

# Criar docker-compose temporário
cat > docker-compose-cert-test.yml << EOF
version: '3.8'
services:
  nginx-cert-test:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx-temp.conf:/etc/nginx/nginx.conf:ro
      - certbot-web:/var/www/certbot
    command: nginx -g 'daemon off;'
    networks:
      - cert-test-network

volumes:
  certbot-web:

networks:
  cert-test-network:
    driver: bridge
EOF

# Iniciar nginx
echo "🚀 Iniciando nginx..."
docker-compose -f docker-compose-cert-test.yml up -d

# Aguardar nginx estar pronto
echo "⏳ Aguardando nginx..."
sleep 10

# Testar nginx
echo "🔍 Testando nginx..."
if curl -s http://localhost > /dev/null; then
    echo "✅ Nginx respondendo"
else
    echo "❌ Nginx não está respondendo"
    docker-compose -f docker-compose-cert-test.yml down
    rm -f nginx-temp.conf docker-compose-cert-test.yml
    exit 1
fi

# Testar webroot
echo "🔍 Testando webroot..."
mkdir -p /tmp/certbot-test
echo "test" > /tmp/certbot-test/test.txt
docker run --rm -v /tmp/certbot-test:/var/www/certbot/test nginx:alpine cp /var/www/certbot/test/test.txt /var/www/certbot/

if curl -s http://localhost/.well-known/acme-challenge/test.txt | grep -q "test"; then
    echo "✅ Webroot funcionando"
else
    echo "❌ Webroot não está funcionando"
fi

# Tentar emissão de certificado
echo "📜 Tentando emissão de certificado..."
docker run --rm \
    -v ssl-certs:/etc/letsencrypt \
    -v certbot-web:/var/www/certbot \
    certbot/certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email "$EMAIL" \
    --agree-tos \
    --no-eff-email \
    --non-interactive \
    --dry-run \
    -d "$DOMAIN"

if [ $? -eq 0 ]; then
    echo "✅ Teste de certificado bem-sucedido!"
    echo "💡 Agora você pode executar o deploy completo"
else
    echo "❌ Teste de certificado falhou"
    echo "💡 Verifique:"
    echo "   - DNS apontando para este servidor"
    echo "   - Portas 80 e 443 liberadas"
    echo "   - Domínio acessível externamente"
fi

# Limpeza
echo "🧹 Limpando..."
docker-compose -f docker-compose-cert-test.yml down
rm -f nginx-temp.conf docker-compose-cert-test.yml
rm -rf /tmp/certbot-test

echo "✅ Teste concluído!" 