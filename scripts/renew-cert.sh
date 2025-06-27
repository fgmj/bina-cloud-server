#!/bin/bash

# Configurações
DOMAIN="bina.fernandojunior.com.br"
EMAIL="fernando.medeiros@gmail.com"
CERTS_VOLUME="ssl-certs"
WEBROOT_VOLUME="certbot-web"
LOG_FILE="/var/log/certbot-renewal.log"
MAX_RETRIES=3

# Função para log
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# Função para verificar se o certificado expira em menos de 30 dias
check_cert_expiry() {
    local cert_file="/etc/letsencrypt/live/$DOMAIN/fullchain.pem"
    
    if [ ! -f "$cert_file" ]; then
        log "❌ Certificado não encontrado: $cert_file"
        return 1
    fi
    
    local expiry_date=$(openssl x509 -enddate -noout -in "$cert_file" | cut -d= -f2)
    local expiry_timestamp=$(date -d "$expiry_date" +%s)
    local current_timestamp=$(date +%s)
    local days_until_expiry=$(( (expiry_timestamp - current_timestamp) / 86400 ))
    
    log "📅 Certificado expira em $days_until_expiry dias"
    
    if [ $days_until_expiry -lt 30 ]; then
        log "⚠️  Certificado expira em menos de 30 dias. Renovação necessária."
        return 0
    else
        log "✅ Certificado ainda válido por mais de 30 dias."
        return 1
    fi
}

# Função para renovar certificado
renew_certificate() {
    local retry_count=0
    
    while [ $retry_count -lt $MAX_RETRIES ]; do
        log "🔄 Tentativa de renovação $((retry_count + 1))/$MAX_RETRIES"
        
        docker run --rm \
            -v "${CERTS_VOLUME}:/etc/letsencrypt" \
            -v "${WEBROOT_VOLUME}:/var/www/certbot" \
            certbot/certbot renew \
            --webroot \
            --webroot-path=/var/www/certbot \
            --quiet \
            --non-interactive \
            --agree-tos \
            --email "$EMAIL"
        
        if [ $? -eq 0 ]; then
            log "✅ Certificado renovado com sucesso!"
            
            # Recarregar nginx se estiver rodando
            if docker ps | grep -q "nginx"; then
                log "🔄 Recarregando configuração do nginx..."
                docker exec nginx nginx -s reload
                log "✅ Nginx recarregado com sucesso!"
            fi
            
            return 0
        else
            retry_count=$((retry_count + 1))
            log "❌ Falha na tentativa $retry_count. Aguardando 60 segundos..."
            sleep 60
        fi
    done
    
    log "❌ Falha em todas as tentativas de renovação"
    return 1
}

# Função para verificar conectividade
check_connectivity() {
    log "🌐 Verificando conectividade..."
    
    # Verificar se o domínio responde
    if ! nslookup "$DOMAIN" >/dev/null 2>&1; then
        log "❌ DNS não resolve para $DOMAIN"
        return 1
    fi
    
    # Verificar se as portas 80 e 443 estão acessíveis
    if ! nc -z "$DOMAIN" 80 2>/dev/null; then
        log "❌ Porta 80 não está acessível"
        return 1
    fi
    
    if ! nc -z "$DOMAIN" 443 2>/dev/null; then
        log "❌ Porta 443 não está acessível"
        return 1
    fi
    
    log "✅ Conectividade OK"
    return 0
}

# Função para backup dos certificados
backup_certificates() {
    local backup_dir="/backup/certs/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$backup_dir"
    
    log "💾 Fazendo backup dos certificados em $backup_dir"
    
    if [ -d "/etc/letsencrypt/live/$DOMAIN" ]; then
        cp -r "/etc/letsencrypt/live/$DOMAIN" "$backup_dir/"
        log "✅ Backup concluído"
    else
        log "⚠️  Diretório de certificados não encontrado para backup"
    fi
}

# Função principal
main() {
    log "🚀 Iniciando verificação de renovação de certificados"
    
    # Verificar conectividade
    if ! check_connectivity; then
        log "❌ Problemas de conectividade detectados"
        exit 1
    fi
    
    # Verificar se precisa renovar
    if check_cert_expiry; then
        log "🔄 Iniciando processo de renovação"
        
        # Backup antes da renovação
        backup_certificates
        
        # Tentar renovar
        if renew_certificate; then
            log "🎉 Renovação concluída com sucesso!"
            exit 0
        else
            log "❌ Falha na renovação"
            exit 1
        fi
    else
        log "✅ Renovação não necessária"
        exit 0
    fi
}

# Executar função principal
main "$@" 