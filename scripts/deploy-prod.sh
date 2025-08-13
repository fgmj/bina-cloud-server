#!/bin/bash

# Configurações (respeita variáveis exportadas pelo ambiente/GitHub Actions)
# - Se NGINX_HOST já estiver setado fora, usa-o; senão, usa default
# - Se CERTBOT_EMAIL já estiver setado fora, usa-o; senão, usa default
# - Perfil Spring será derivado de SPRING_PROFILES_ACTIVE, ENV (ex: PROD) ou 'prod'
DOMAIN="${NGINX_HOST:-bina.fernandojunior.com.br}"
EMAIL="${CERTBOT_EMAIL:-fernando.medeiros@gmail.com}"
ENVIRONMENT_DEFAULT="prod"
BACKUP_DIR="./backup/$(date +%Y%m%d_%H%M%S)"
DC=""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log colorido
log() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

success() {
    echo -e "${GREEN}✅ $1${NC}"
}

warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

error() {
    echo -e "${RED}❌ $1${NC}"
}

# Função para verificar pré-requisitos
check_prerequisites() {
    log "🔍 Verificando pré-requisitos..."
    
    # Verificar se Docker está instalado
    if ! command -v docker &> /dev/null; then
        error "Docker não está instalado"
        exit 1
    fi
    
    # Resolver comando docker compose (v2 plugin ou v1 binário)
    if docker compose version >/dev/null 2>&1; then
        DC="docker compose"
        success "Docker Compose (plugin v2) detectado"
    elif command -v docker-compose >/dev/null 2>&1; then
        DC="docker-compose"
        success "Docker Compose (binário v1) detectado"
    else
        error "Docker Compose não está instalado (nem plugin 'docker compose' nem binário 'docker-compose')"
        exit 1
    fi
    
    # Verificar se o domínio resolve
    if ! nslookup "$DOMAIN" &> /dev/null; then
        error "Domínio $DOMAIN não resolve"
        exit 1
    fi
    
    # Verificar se as portas estão livres (usar ss no Ubuntu)
    if command -v ss &> /dev/null; then
        if ss -tuln | grep -q ":80 "; then
            warning "Porta 80 já está em uso"
        fi
        
        if ss -tuln | grep -q ":443 "; then
            warning "Porta 443 já está em uso"
        fi
    else
        # Fallback para netstat se ss não estiver disponível
        if command -v netstat &> /dev/null; then
            if netstat -tuln | grep -q ":80 "; then
                warning "Porta 80 já está em uso"
            fi
            
            if netstat -tuln | grep -q ":443 "; then
                warning "Porta 443 já está em uso"
            fi
        else
            warning "Não foi possível verificar portas (ss/netstat não encontrado)"
        fi
    fi
    
    success "Pré-requisitos verificados"
}

# Função para backup
create_backup() {
    log "💾 Criando backup..."
    
    mkdir -p "$BACKUP_DIR"
    
    # Backup dos volumes Docker
    if docker volume ls | grep -q "ssl-certs"; then
        docker run --rm -v ssl-certs:/data -v "$(pwd)/$BACKUP_DIR":/backup alpine tar czf /backup/ssl-certs-backup.tar.gz -C /data .
        success "Backup SSL criado"
    fi
    
    if docker volume ls | grep -q "h2-data"; then
        docker run --rm -v h2-data:/data -v "$(pwd)/$BACKUP_DIR":/backup alpine tar czf /backup/h2-data-backup.tar.gz -C /data .
        success "Backup dados criado"
    fi
    
    # Backup do docker-compose.yml
    cp docker-compose.yml "$BACKUP_DIR/"
    cp -r nginx "$BACKUP_DIR/"
    cp -r scripts "$BACKUP_DIR/"
    
    success "Backup completo criado em $BACKUP_DIR"
}

# Função para parar serviços
stop_services() {
    log "🛑 Parando serviços..."
    
    $DC down --remove-orphans
    
    success "Serviços parados"
}

# Função para iniciar certificados
init_certificates() {
    log "🔐 Inicializando certificados SSL..."
    
    # Verificar se já existe certificado
    if docker volume ls | grep -q "ssl-certs" && docker run --rm -v ssl-certs:/etc/letsencrypt alpine ls /etc/letsencrypt/live/"$DOMAIN" &> /dev/null; then
        warning "Certificado já existe. Pulando inicialização."
        return 0
    fi
    
    # Criar volumes se não existirem
    docker volume create ssl-certs 2>/dev/null || true
    docker volume create certbot-web 2>/dev/null || true   
        
    # Emitir certificado
    log "📜 Emitindo certificado Let's Encrypt..."
    docker run --rm -p 80:80 \
     -v ssl-certs:/etc/letsencrypt \
     -v certbot-web:/var/www/certbot \
     certbot/certbot certonly --standalone \
     --preferred-challenges http \
     -d $DOMAIN \
     --email $EMAIL --agree-tos --no-eff-email

    
    CERT_RESULT=$?    
    
    if [ $CERT_RESULT -eq 0 ]; then
        success "Certificado emitido com sucesso!"
    else
        error "Falha ao emitir certificado"
        log "💡 Dicas para resolver:"
        log "   1. Verifique se o DNS está apontando para este servidor"
        log "   2. Verifique se as portas 80 e 443 estão liberadas no firewall"
        log "   3. Verifique se o domínio está acessível externamente"
        log "   4. Tente novamente em alguns minutos"
        exit 1
    fi
}

# Função para iniciar serviços
start_services() {
    log "🚀 Iniciando serviços..."
    
    

    # Respeitar NGINX_HOST/CERTBOT_EMAIL já exportados; se ausentes, definir
    export NGINX_HOST="${NGINX_HOST:-$DOMAIN}"
    export ENV=PROD
    export CERTBOT_EMAIL="${CERTBOT_EMAIL:-$EMAIL}"
    export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-$ENVIRONMENT_DEFAULT}"
    
    # Iniciar todos os serviços
    $DC up -d --build
    
    # Aguardar serviços estarem prontos
    log "⏳ Aguardando serviços estarem prontos..."
    sleep 30
    
    # Verificar health checks
    if $DC ps | grep -q "unhealthy"; then
        warning "Alguns serviços não estão saudáveis"
        $DC logs
        log "💡 Verificando logs para diagnóstico..."
    fi
    
    success "Serviços iniciados com sucesso!"
}

# Função para verificar status
check_status() {
    log "🔍 Verificando status dos serviços..."
    
    echo
    echo "=== Status dos Containers ==="
    $DC ps
    
    echo
    echo "=== Logs dos Serviços ==="
    $DC logs --tail=20
    
    echo
    echo "=== Verificação de Conectividade ==="
    
    # Testar HTTP
    if curl -s -o /dev/null -w "%{http_code}" "http://$DOMAIN" | grep -q "301"; then
        success "HTTP redirect funcionando"
    else
        warning "HTTP redirect não está funcionando"
    fi
    
    # Testar HTTPS (pode falhar se certificado não foi emitido)
    if curl -s -o /dev/null -w "%{http_code}" "https://$DOMAIN" 2>/dev/null | grep -q "200"; then
        success "HTTPS funcionando"
    else
        warning "HTTPS não está funcionando (pode ser normal se certificado não foi emitido)"
    fi
    
    # Testar WebSocket
    if curl -s -o /dev/null -w "%{http_code}" "https://$DOMAIN/ws" 2>/dev/null | grep -q "404\|200"; then
        success "WebSocket endpoint acessível"
    else
        warning "WebSocket endpoint não está acessível"
    fi
    
    echo
    echo "=== Informações do Certificado ==="
    if docker run --rm -v ssl-certs:/etc/letsencrypt alpine ls /etc/letsencrypt/live/"$DOMAIN" &> /dev/null; then
        docker run --rm -v ssl-certs:/etc/letsencrypt alpine openssl x509 -in /etc/letsencrypt/live/"$DOMAIN"/fullchain.pem -text -noout | grep -E "(Subject:|Not After:)"
    else
        warning "Certificado não encontrado"
    fi
}

# Função para mostrar informações de monitoramento
show_monitoring_info() {
    echo
    echo "=== Informações de Monitoramento ==="
    echo "🔍 Logs em tempo real: $DC logs -f"
    echo "📊 Status dos containers: $DC ps"
    echo "🔐 Verificar certificado: docker run --rm -v ssl-certs:/etc/letsencrypt alpine openssl x509 -in /etc/letsencrypt/live/$DOMAIN/fullchain.pem -text -noout"
    echo "🔄 Renovar certificado manualmente: $DC exec certbot certbot renew"
    echo "📝 Logs do nginx: $DC logs nginx"
    echo "📝 Logs da aplicação: $DC logs app"
    echo
    echo "=== URLs de Acesso ==="
    echo "🌐 Aplicação: https://$DOMAIN"
    echo "📊 Monitor: https://$DOMAIN/monitor"
    echo "📋 Eventos: https://$DOMAIN/eventos"
    echo "🔧 H2 Console: https://$DOMAIN/h2-console"
    echo "📚 Swagger: https://$DOMAIN/swagger-ui.html"
}

# Função principal
main() {
    echo "🚀 Deploy de Produção - Bina Cloud Server"
    echo "=========================================="
    echo "Domínio: $DOMAIN"
    echo "Email: $EMAIL"
    echo "Ambiente: $ENVIRONMENT"
    echo
    
    # Verificar se está rodando como root
    if [ "$EUID" -eq 0 ]; then
        warning "Executando como root. Certifique-se de que isso é necessário."
    fi
    
    # Executar etapas
    check_prerequisitos_ok=true
    check_prerequisites || check_prerequisitos_ok=false
    if [ "$check_prerequisitos_ok" = false ]; then
        exit 1
    fi

    # Atualizar repositório (espelha script funcional)
    log "📥 Atualizando código fonte (git fetch/reset/pull)..."
    if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
        git fetch origin && git reset --hard origin/main && git pull origin main || {
            warning "Falha ao atualizar via git; prosseguindo mesmo assim"
        }
    else
        warning "Diretório não é um repositório git; pulando update"
    fi

    create_backup
    stop_services
    init_certificates
    start_services
    check_status
    show_monitoring_info
    
    echo
    success "Deploy concluído com sucesso!"
    echo "🎉 A aplicação está disponível em https://$DOMAIN"
}

# Executar função principal
main "$@" 