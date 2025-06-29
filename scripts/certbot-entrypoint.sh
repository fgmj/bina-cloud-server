#!/bin/bash

# Certbot automatic renewal entrypoint script
# This script runs inside the certbot container and handles automatic certificate renewal

set -e

# Configuration
CERTBOT_EMAIL="${CERTBOT_EMAIL:-fernando.medeiros@gmail.com}"
DOMAIN="${DOMAIN:-bina.fernandojunior.com.br}"
WEBROOT_PATH="/var/www/certbot"
RENEWAL_INTERVAL="${RENEWAL_INTERVAL:-12h}"

# Function to log messages
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Function to check if certificates exist
check_certificates() {
    if [ -f "/etc/letsencrypt/live/$DOMAIN/fullchain.pem" ]; then
        log "✅ Certificates found for domain: $DOMAIN"
        return 0
    else
        log "⚠️ No certificates found for domain: $DOMAIN"
        return 1
    fi
}

# Function to obtain initial certificates
obtain_certificates() {
    log "🔐 Obtaining initial certificates for domain: $DOMAIN"
    
    certbot certonly \
        --webroot \
        --webroot-path="$WEBROOT_PATH" \
        --email "$CERTBOT_EMAIL" \
        --agree-tos \
        --no-eff-email \
        --non-interactive \
        --quiet \
        -d "$DOMAIN"
    
    if [ $? -eq 0 ]; then
        log "✅ Initial certificates obtained successfully"
        return 0
    else
        log "❌ Failed to obtain initial certificates"
        return 1
    fi
}

# Function to renew certificates
renew_certificates() {
    log "🔄 Renewing certificates..."
    
    certbot renew \
        --webroot \
        --webroot-path="$WEBROOT_PATH" \
        --quiet \
        --non-interactive \
        --agree-tos \
        --email "$CERTBOT_EMAIL"
    
    if [ $? -eq 0 ]; then
        log "✅ Certificates renewed successfully"
        
        # Reload nginx if it's running
        if docker exec nginx nginx -s reload 2>/dev/null; then
            log "✅ Nginx configuration reloaded"
        else
            log "⚠️ Failed to reload nginx configuration"
        fi
        
        return 0
    else
        log "❌ Failed to renew certificates"
        return 1
    fi
}

# Function to check certificate expiry
check_expiry() {
    local cert_file="/etc/letsencrypt/live/$DOMAIN/fullchain.pem"
    
    if [ ! -f "$cert_file" ]; then
        log "❌ Certificate file not found: $cert_file"
        return 1
    fi
    
    local expiry_date=$(openssl x509 -enddate -noout -in "$cert_file" | cut -d= -f2)
    local expiry_timestamp=$(date -d "$expiry_date" +%s)
    local current_timestamp=$(date +%s)
    local days_until_expiry=$(( (expiry_timestamp - current_timestamp) / 86400 ))
    
    log "📅 Certificate expires in $days_until_expiry days"
    
    if [ $days_until_expiry -lt 30 ]; then
        log "⚠️ Certificate expires in less than 30 days. Renewal needed."
        return 0
    else
        log "✅ Certificate is still valid for more than 30 days."
        return 1
    fi
}

# Main function
main() {
    log "🚀 Starting certbot automatic renewal service"
    log "📧 Email: $CERTBOT_EMAIL"
    log "🌐 Domain: $DOMAIN"
    log "⏰ Renewal interval: $RENEWAL_INTERVAL"
    
    # Check if certificates exist, if not obtain them
    if ! check_certificates; then
        log "🔐 No certificates found. Attempting to obtain initial certificates..."
        if ! obtain_certificates; then
            log "❌ Failed to obtain initial certificates. Exiting."
            exit 1
        fi
    fi
    
    # Main renewal loop
    log "🔄 Starting automatic renewal loop..."
    while true; do
        log "⏰ Waiting $RENEWAL_INTERVAL before next renewal check..."
        sleep "$RENEWAL_INTERVAL"
        
        log "🔄 Checking certificate renewal..."
        
        # Check if renewal is needed
        if check_expiry; then
            renew_certificates
        else
            log "✅ No renewal needed at this time"
        fi
    done
}

# Handle signals gracefully
trap 'log "🛑 Received signal, shutting down..."; exit 0' SIGTERM SIGINT

# Run main function
main "$@" 