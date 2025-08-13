# 🔐 Configuração SSL/HTTPS - Bina Cloud Server

## 📋 Visão Geral

Este documento descreve a configuração completa de SSL/HTTPS para o Bina Cloud Server, incluindo:
- Certificados Let's Encrypt
- Renovação automática
- Configurações de segurança
- Monitoramento

## 🏗️ Arquitetura

```
Internet → Nginx (80/443) → Spring Boot App (8080)
                ↓
            Certbot (Renovação automática)
                ↓
            Let's Encrypt (Certificados)
```

## 📁 Estrutura de Arquivos

```
bina-cloud-server/
├── docker-compose.yml          # Orquestração dos serviços
├── nginx/
│   ├── Dockerfile             # Imagem do Nginx
│   └── nginx.conf             # Configuração do Nginx
├── scripts/
│   ├── init-cert.sh           # Inicialização de certificados
│   ├── renew-cert.sh          # Renovação manual
│   └── deploy-prod.sh         # Deploy completo
└── SSL_CONFIGURATION.md       # Esta documentação
```

## 🔧 Configurações Implementadas

### 1. Docker Compose

**Serviços configurados:**
- **app**: Aplicação Spring Boot
- **nginx**: Proxy reverso com SSL
- **certbot**: Renovação automática de certificados
- **cert-monitor**: Monitoramento de certificados

**Volumes:**
- `ssl-certs`: Certificados Let's Encrypt
- `certbot-web`: Webroot para validação
- `nginx-logs`: Logs do Nginx
- `certbot-logs`: Logs do Certbot

### 2. Nginx Configuration

**Recursos de Segurança:**
- ✅ HTTP/2 habilitado
- ✅ HSTS (Strict Transport Security)
- ✅ Headers de segurança (X-Frame-Options, CSP, etc.)
- ✅ Rate limiting
- ✅ OCSP Stapling
- ✅ Ciphers modernos (TLS 1.2/1.3)

**Headers de Segurança:**
```nginx
add_header X-Frame-Options "SAMEORIGIN" always;
add_header X-Content-Type-Options "nosniff" always;
add_header X-XSS-Protection "1; mode=block" always;
add_header Referrer-Policy "strict-origin-when-cross-origin" always;
add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;
add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; font-src 'self' https://cdn.jsdelivr.net; img-src 'self' data: https:; connect-src 'self' wss: ws:; frame-ancestors 'self';" always;
```

### 3. Certificados Let's Encrypt

**Configuração:**
- **Domínio**: `bina.fernandojunior.com.br`
- **Email**: `fernando.medeiros@gmail.com`
- **Validade**: 90 dias
- **Renovação**: Automática a cada 12 horas

**Arquivos de certificado:**
```
/etc/letsencrypt/live/bina.fernandojunior.com.br/
├── fullchain.pem      # Certificado completo
├── privkey.pem        # Chave privada
└── chain.pem          # Cadeia de certificados
```

## 🚀 Deploy em Produção

### Pré-requisitos

1. **DNS configurado** apontando para o servidor
2. **Portas 80 e 443** liberadas no firewall
3. **Docker e Docker Compose** instalados
4. **Domínio válido** (não localhost)

### Passos para Deploy

1. **Clonar o repositório:**
```bash
git clone <repository>
cd bina-cloud-server
```

2. **Configurar variáveis de ambiente:**
```bash
export NGINX_HOST=bina.fernandojunior.com.br
export CERTBOT_EMAIL=fernando.medeiros@gmail.com
export SPRING_PROFILES_ACTIVE=prod
```

3. **Executar deploy:**
```bash
chmod +x scripts/deploy-prod.sh
./scripts/deploy-prod.sh
```

### Verificação do Deploy

```bash
# Status dos containers
docker-compose ps

# Logs em tempo real
docker-compose logs -f

# Verificar certificado
docker run --rm -v ssl-certs:/etc/letsencrypt alpine openssl x509 -in /etc/letsencrypt/live/bina.fernandojunior.com.br/fullchain.pem -text -noout
```

## 🔄 Renovação de Certificados

### Renovação Automática

O Certbot executa automaticamente a cada 12 horas:
```bash
certbot renew --webroot -w /var/www/certbot --quiet --non-interactive
```

### Renovação Manual

```bash
# Renovar certificados
docker-compose exec certbot certbot renew

# Recarregar nginx após renovação
docker-compose exec nginx nginx -s reload
```

### Script de Renovação Robusto

```bash
chmod +x scripts/renew-cert.sh
./scripts/renew-cert.sh
```

**Recursos do script:**
- ✅ Verificação de conectividade
- ✅ Backup antes da renovação
- ✅ Retry com backoff exponencial
- ✅ Recarregamento automático do nginx
- ✅ Logs detalhados

## 📊 Monitoramento

### Monitor de Certificados

O serviço `cert-monitor` verifica diariamente:
- Validade dos certificados
- Dias restantes até expiração
- Alertas quando < 7 dias

### Logs de Monitoramento

```bash
# Logs do nginx
docker-compose logs nginx

# Logs do certbot
docker-compose logs certbot

# Logs do monitor
docker-compose logs cert-monitor
```

### Health Checks

```bash
# Health check da aplicação
curl -f https://bina.fernandojunior.com.br/actuator/health

# Health check do nginx
curl -f http://localhost/health
```

## 🔒 Configurações de Segurança

### SSL/TLS

- **Protocolos**: TLS 1.2, TLS 1.3
- **Ciphers**: ECDHE com AES-GCM e ChaCha20-Poly1305
- **OCSP Stapling**: Habilitado
- **HSTS**: 2 anos com preload

### Rate Limiting

```nginx
# API endpoints: 10 req/s com burst de 20
limit_req zone=api burst=20 nodelay;

# Login endpoints: 1 req/s
limit_req zone=login burst=5 nodelay;
```

### Headers de Segurança

- **X-Frame-Options**: SAMEORIGIN
- **X-Content-Type-Options**: nosniff
- **X-XSS-Protection**: 1; mode=block
- **Referrer-Policy**: strict-origin-when-cross-origin
- **Content-Security-Policy**: Configurado para CDNs permitidos

## 🛠️ Troubleshooting

### Problemas Comuns

1. **Certificado não emitido:**
```bash
# Verificar DNS
nslookup bina.fernandojunior.com.br

# Verificar portas
netstat -tuln | grep -E ":80|:443"

# Logs do certbot
docker-compose logs certbot
```

2. **Nginx não inicia:**
```bash
# Verificar configuração
docker-compose exec nginx nginx -t

# Logs do nginx
docker-compose logs nginx
```

3. **Renovação falha:**
```bash
# Verificar conectividade
docker-compose exec certbot ping bina.fernandojunior.com.br

# Tentar renovação manual
docker-compose exec certbot certbot renew --dry-run
```

### Comandos Úteis

```bash
# Verificar status dos certificados
docker run --rm -v ssl-certs:/etc/letsencrypt alpine ls -la /etc/letsencrypt/live/

# Verificar validade
docker run --rm -v ssl-certs:/etc/letsencrypt alpine openssl x509 -in /etc/letsencrypt/live/bina.fernandojunior.com.br/fullchain.pem -text -noout | grep -E "(Subject:|Not After:)"

# Backup dos certificados
docker run --rm -v ssl-certs:/data -v $(pwd):/backup alpine tar czf /backup/ssl-backup-$(date +%Y%m%d).tar.gz -C /data .

# Restaurar certificados
docker run --rm -v ssl-certs:/data -v $(pwd):/backup alpine tar xzf /backup/ssl-backup-YYYYMMDD.tar.gz -C /data
```

## 📈 Performance

### Otimizações Implementadas

- **HTTP/2**: Habilitado para melhor performance
- **Gzip**: Compressão para arquivos estáticos
- **Cache**: Headers de cache para recursos estáticos
- **Keep-alive**: Conexões persistentes
- **Buffer tuning**: Buffers otimizados para throughput

### Métricas de Performance

```bash
# Teste de performance
ab -n 1000 -c 10 https://bina.fernandojunior.com.br/

# Teste de SSL
openssl s_client -connect bina.fernandojunior.com.br:443 -servername bina.fernandojunior.com.br
```

## 🔄 Backup e Recuperação

### Backup Automático

O script de deploy cria backups automáticos:
- Certificados SSL
- Dados da aplicação
- Configurações

### Restauração

```bash
# Restaurar certificados
docker run --rm -v ssl-certs:/data -v /backup:/backup alpine tar xzf /backup/ssl-certs-backup.tar.gz -C /data

# Restaurar dados
docker run --rm -v h2-data:/data -v /backup:/backup alpine tar xzf /backup/h2-data-backup.tar.gz -C /data
```

## 📞 Suporte

### Contatos

- **Email**: fernando.medeiros@gmail.com
- **Domínio**: bina.fernandojunior.com.br

### Logs Importantes

- **Nginx**: `/var/log/nginx/`
- **Certbot**: `/var/log/letsencrypt/`
- **Aplicação**: `docker-compose logs app`

### Alertas

Configure webhooks para receber alertas sobre:
- Falha na renovação de certificados
- Certificado próximo do vencimento
- Problemas de conectividade

---

**Última atualização**: Junho 2025
