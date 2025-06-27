#!/bin/bash

DOMAIN="bina.fernandojunior.com.br"
EMAIL="fernando.medeiros@gmail.com"

echo "🔐 Teste de Validação ACME - Let's Encrypt"
echo "=========================================="
echo "Domínio: $DOMAIN"
echo "Email: $EMAIL"
echo

# Parar qualquer container que possa estar usando a porta 80
echo "🛑 Parando containers existentes..."
docker-compose down 2>/dev/null || true
docker stop $(docker ps -q --filter "publish=80") 2>/dev/null || true

# Criar volumes se não existirem
echo "🔧 Criando volumes..."
docker volume create ssl-certs 2>/dev/null || echo "Volume ssl-certs já existe"
docker volume create certbot-web 2>/dev/null || echo "Volume certbot-web já existe"

# Criar nginx simples para validação
echo "🚀 Criando nginx para validação..."
cat > nginx-acme-test.conf << 'EOF'
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    server {
        listen 80;
        server_name bina.fernandojunior.com.br;
        
        # ACME Challenge
        location /.well-known/acme-challenge/ {
            root /var/www/certbot;
            try_files $uri =404;
        }
        
        # Health check
        location /health {
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
        
        # Default
        location / {
            return 200 "Nginx ACME Test Server\n";
            add_header Content-Type text/plain;
        }
    }
}
EOF

# Criar docker-compose para teste
cat > docker-compose-acme-test.yml << EOF
version: '3.8'
services:
  nginx-acme-test:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx-acme-test.conf:/etc/nginx/nginx.conf:ro
      - certbot-web:/var/www/certbot
    command: nginx -g 'daemon off;'
    networks:
      - acme-test-network

volumes:
  certbot-web:

networks:
  acme-test-network:
    driver: bridge
EOF

# Iniciar nginx
echo "🚀 Iniciando nginx..."
docker-compose -f docker-compose-acme-test.yml up -d

# Aguardar nginx estar pronto
echo "⏳ Aguardando nginx..."
sleep 10

# Testar nginx localmente
echo "🔍 Testando nginx localmente..."
if curl -s http://localhost/health | grep -q "healthy"; then
    echo "✅ Nginx respondendo localmente"
else
    echo "❌ Nginx não está respondendo localmente"
    docker-compose -f docker-compose-acme-test.yml logs
    docker-compose -f docker-compose-acme-test.yml down
    rm -f nginx-acme-test.conf docker-compose-acme-test.yml
    exit 1
fi

# Testar webroot
echo "🔍 Testando webroot..."
mkdir -p /tmp/acme-test
echo "test-content" > /tmp/acme-test/test.txt
docker run --rm -v /tmp/acme-test:/var/www/certbot/test nginx:alpine cp /var/www/certbot/test/test.txt /var/www/certbot/

if curl -s http://localhost/.well-known/acme-challenge/test.txt | grep -q "test-content"; then
    echo "✅ Webroot funcionando localmente"
else
    echo "❌ Webroot não está funcionando localmente"
fi

# Testar externamente
echo "🔍 Testando conectividade externa..."
EXTERNAL_IP=$(curl -s -m 5 ifconfig.me 2>/dev/null || echo "Não foi possível obter IP externo")
echo "   IP externo: $EXTERNAL_IP"

if curl -s -m 10 "http://$DOMAIN/health" | grep -q "healthy"; then
    echo "✅ Nginx acessível externamente"
else
    echo "❌ Nginx não acessível externamente"
    echo "   💡 Verifique se o DNS está apontando para $EXTERNAL_IP"
fi

# Testar webroot externamente
if curl -s -m 10 "http://$DOMAIN/.well-known/acme-challenge/test.txt" | grep -q "test-content"; then
    echo "✅ Webroot acessível externamente"
else
    echo "❌ Webroot não acessível externamente"
fi

# Tentar emissão de certificado (dry-run)
echo "📜 Tentando emissão de certificado (dry-run)..."
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

CERT_RESULT=$?

if [ $CERT_RESULT -eq 0 ]; then
    echo "✅ Teste de certificado bem-sucedido!"
    echo "💡 Agora você pode executar o deploy completo"
else
    echo "❌ Teste de certificado falhou"
    echo "💡 Logs do certbot:"
    docker run --rm -v ssl-certs:/etc/letsencrypt alpine cat /etc/letsencrypt/letsencrypt.log | tail -20
fi

# Limpeza
echo "🧹 Limpando..."
docker-compose -f docker-compose-acme-test.yml down
rm -f nginx-acme-test.conf docker-compose-acme-test.yml
rm -rf /tmp/acme-test

echo "✅ Teste ACME concluído!" 