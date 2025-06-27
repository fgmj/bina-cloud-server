#!/bin/bash

echo "🔍 Teste Simples do Webroot"
echo "==========================="

# Parar containers existentes
echo "🛑 Parando containers..."
docker-compose down 2>/dev/null || true
docker stop $(docker ps -q --filter "publish=80") 2>/dev/null || true

# Criar volume
echo "🔧 Criando volume..."
docker volume create certbot-web 2>/dev/null || echo "Volume já existe"

# Criar nginx simples
echo "🚀 Criando nginx..."
cat > nginx-simple.conf << 'EOF'
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    # Logs
    access_log /var/log/nginx/access.log;
    error_log /var/log/nginx/error.log;

    # Basic settings
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;

    server {
        listen 80 default_server;
        server_name bina.fernandojunior.com.br _;
        
        # ACME Challenge - Let's Encrypt validation
        location /.well-known/acme-challenge/ {
            root /var/www/certbot;
            try_files $uri =404;
            access_log off;
            log_not_found off;
        }
        
        # Health check
        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
        
        # Default response
        location / {
            return 200 "Nginx OK - Webroot Test Server\n";
            add_header Content-Type text/plain;
        }
    }
}
EOF

# Iniciar nginx
echo "🚀 Iniciando nginx..."
docker run -d --name nginx-test \
    -p 80:80 \
    -v $(pwd)/nginx-simple.conf:/etc/nginx/nginx.conf:ro \
    -v certbot-web:/var/www/certbot \
    nginx:alpine nginx -g 'daemon off;'

sleep 5

# Testar nginx
echo "🔍 Testando nginx..."
if curl -s http://localhost | grep -q "Nginx OK"; then
    echo "✅ Nginx funcionando com configuração personalizada"
else
    echo "❌ Nginx não funcionando ou usando configuração padrão"
    echo "💡 Verificando configuração..."
    docker exec nginx-test nginx -T | head -20
    docker logs nginx-test
    docker stop nginx-test && docker rm nginx-test
    rm -f nginx-simple.conf
    exit 1
fi

# Testar webroot
echo "🔍 Testando webroot..."
echo "test-file" > /tmp/test.txt

# Copiar para o volume
docker run --rm \
    -v certbot-web:/var/www/certbot \
    -v /tmp/test.txt:/tmp/test.txt \
    alpine sh -c "cp /tmp/test.txt /var/www/certbot/ && chmod 644 /var/www/certbot/test.txt"

# Verificar se foi copiado
echo "📁 Conteúdo do volume:"
docker run --rm -v certbot-web:/var/www/certbot alpine ls -la /var/www/certbot/

# Testar acesso via nginx
if curl -s http://localhost/.well-known/acme-challenge/test.txt | grep -q "test-file"; then
    echo "✅ Webroot funcionando!"
else
    echo "❌ Webroot não funcionando"
    echo "💡 Logs do nginx:"
    docker logs nginx-test
fi

# Testar externamente
echo "🔍 Testando externamente..."
if curl -s -m 10 "http://bina.fernandojunior.com.br/.well-known/acme-challenge/test.txt" | grep -q "test-file"; then
    echo "✅ Webroot acessível externamente!"
else
    echo "❌ Webroot não acessível externamente"
fi

# Limpeza
echo "🧹 Limpando..."
docker stop nginx-test && docker rm nginx-test
rm -f nginx-simple.conf /tmp/test.txt

echo "✅ Teste concluído!" 