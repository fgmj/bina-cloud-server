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
events { worker_connections 1024; }
http {
    server {
        listen 80;
        server_name bina.fernandojunior.com.br;
        
        location /.well-known/acme-challenge/ {
            root /var/www/certbot;
            try_files $uri =404;
        }
        
        location / {
            return 200 "OK\n";
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
    nginx:alpine

sleep 5

# Testar nginx
echo "🔍 Testando nginx..."
if curl -s http://localhost | grep -q "OK"; then
    echo "✅ Nginx funcionando"
else
    echo "❌ Nginx não funcionando"
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