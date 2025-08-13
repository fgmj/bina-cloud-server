# 🔐 Configuração SSL/HTTPS - Windows

## 📋 Resumo da Revisão

Esta revisão implementou uma configuração completa e robusta de SSL/HTTPS para o Bina Cloud Server, incluindo:

### ✅ **Configurações Implementadas**

1. **Docker Compose Aprimorado**
   - Serviços: app, nginx, certbot, cert-monitor
   - Volumes persistentes para certificados
   - Health checks configurados
   - Logs estruturados

2. **Nginx Otimizado**
   - HTTP/2 habilitado
   - Headers de segurança robustos
   - Rate limiting configurado
   - OCSP Stapling
   - Ciphers modernos (TLS 1.2/1.3)

3. **Certificados Let's Encrypt**
   - Renovação automática a cada 12h
   - Monitoramento de validade
   - Backup automático
   - Scripts de renovação robustos

4. **Scripts de Automação**
   - `deploy-prod.sh`: Deploy completo
   - `renew-cert.sh`: Renovação manual
   - `init-cert.sh`: Inicialização de certificados

### 🔧 **Configurações de Segurança**

#### **Headers de Segurança**
```nginx
X-Frame-Options: SAMEORIGIN
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Referrer-Policy: strict-origin-when-cross-origin
Strict-Transport-Security: max-age=63072000; includeSubDomains; preload
Content-Security-Policy: Configurado para CDNs permitidos
```

#### **SSL/TLS**
- **Protocolos**: TLS 1.2, TLS 1.3
- **Ciphers**: ECDHE com AES-GCM e ChaCha20-Poly1305
- **OCSP Stapling**: Habilitado
- **HSTS**: 2 anos com preload

#### **Rate Limiting**
- **API**: 10 req/s com burst de 20
- **Login**: 1 req/s com burst de 5

### 🚀 **Como Usar no Windows**

#### **1. Pré-requisitos**
```powershell
# Verificar se Docker Desktop está instalado
docker --version
docker-compose --version

# Verificar se o domínio resolve
nslookup bina.fernandojunior.com.br
```

#### **2. Configurar Variáveis de Ambiente**
```powershell
$env:NGINX_HOST = "bina.fernandojunior.com.br"
$env:CERTBOT_EMAIL = "fernando.medeiros@gmail.com"
$env:SPRING_PROFILES_ACTIVE = "prod"
```

#### **3. Deploy em Produção**
```powershell
# Iniciar todos os serviços
docker-compose up -d

# Verificar status
docker-compose ps

# Ver logs
docker-compose logs -f
```

#### **4. Verificar Certificados**
```powershell
# Verificar se o certificado foi emitido
docker run --rm -v ssl-certs:/etc/letsencrypt alpine ls /etc/letsencrypt/live/bina.fernandojunior.com.br/

# Verificar validade
docker run --rm -v ssl-certs:/etc/letsencrypt alpine openssl x509 -in /etc/letsencrypt/live/bina.fernandojunior.com.br/fullchain.pem -text -noout
```

### 📊 **Monitoramento**

#### **Logs Importantes**
```powershell
# Logs do nginx
docker-compose logs nginx

# Logs do certbot
docker-compose logs certbot

# Logs do monitor
docker-compose logs cert-monitor

# Logs da aplicação
docker-compose logs app
```

#### **Health Checks**
```powershell
# Health check da aplicação
Invoke-WebRequest -Uri "https://bina.fernandojunior.com.br/actuator/health" -UseBasicParsing

# Health check do nginx
Invoke-WebRequest -Uri "http://localhost/health" -UseBasicParsing
```

### 🔄 **Renovação de Certificados**

#### **Renovação Automática**
O Certbot executa automaticamente a cada 12 horas.

#### **Renovação Manual**
```powershell
# Renovar certificados
docker-compose exec certbot certbot renew

# Recarregar nginx
docker-compose exec nginx nginx -s reload
```

### 🛠️ **Troubleshooting**

#### **Problemas Comuns**

1. **Certificado não emitido:**
```powershell
# Verificar DNS
nslookup bina.fernandojunior.com.br

# Verificar portas
netstat -an | findstr ":80"
netstat -an | findstr ":443"

# Logs do certbot
docker-compose logs certbot
```

2. **Nginx não inicia:**
```powershell
# Verificar configuração
docker-compose exec nginx nginx -t

# Logs do nginx
docker-compose logs nginx
```

3. **Renovação falha:**
```powershell
# Verificar conectividade
docker-compose exec certbot ping bina.fernandojunior.com.br

# Tentar renovação manual
docker-compose exec certbot certbot renew --dry-run
```

### 📈 **Performance**

#### **Otimizações Implementadas**
- ✅ HTTP/2 habilitado
- ✅ Gzip compression
- ✅ Cache headers para arquivos estáticos
- ✅ Keep-alive connections
- ✅ Buffer tuning otimizado

#### **Testes de Performance**
```powershell
# Teste de SSL
openssl s_client -connect bina.fernandojunior.com.br:443 -servername bina.fernandojunior.com.br
```

### 🔒 **Segurança**

#### **Configurações Implementadas**
- ✅ Headers de segurança robustos
- ✅ Rate limiting configurado
- ✅ Ciphers modernos
- ✅ OCSP Stapling
- ✅ HSTS com preload
- ✅ Content Security Policy

#### **Verificação de Segurança**
```powershell
# Testar headers de segurança
Invoke-WebRequest -Uri "https://bina.fernandojunior.com.br" -UseBasicParsing | Select-Object -ExpandProperty Headers
```

### 📋 **Checklist de Deploy**

- [ ] DNS configurado para o domínio
- [ ] Portas 80 e 443 liberadas no firewall
- [ ] Docker Desktop instalado e rodando
- [ ] Variáveis de ambiente configuradas
- [ ] Certificados emitidos com sucesso
- [ ] Nginx iniciado sem erros
- [ ] Aplicação respondendo em HTTPS
- [ ] WebSocket funcionando via WSS
- [ ] Health checks passando
- [ ] Logs sem erros críticos

### 📞 **Suporte**

- **Email**: fernando.medeiros@gmail.com
- **Domínio**: bina.fernandojunior.com.br
- **Documentação**: `SSL_CONFIGURATION.md`

---

**Última atualização**: Agosto 2025  
**Sistema**: Windows 