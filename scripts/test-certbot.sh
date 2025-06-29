#!/bin/bash

# Test script for certbot configuration
# This script helps diagnose certbot issues

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DOMAIN="${DOMAIN:-bina.fernandojunior.com.br}"
EMAIL="${CERTBOT_EMAIL:-fernando.medeiros@gmail.com}"
CERTS_VOLUME="ssl-certs"
WEBROOT_VOLUME="certbot-web"

# Function to print colored output
print_status() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Function to check if Docker is running
check_docker() {
    print_status $BLUE "🔍 Checking Docker status..."
    if docker info >/dev/null 2>&1; then
        print_status $GREEN "✅ Docker is running"
        return 0
    else
        print_status $RED "❌ Docker is not running or not accessible"
        return 1
    fi
}

# Function to check if volumes exist
check_volumes() {
    print_status $BLUE "🔍 Checking Docker volumes..."
    
    if docker volume ls | grep -q "$CERTS_VOLUME"; then
        print_status $GREEN "✅ SSL certificates volume exists: $CERTS_VOLUME"
    else
        print_status $YELLOW "⚠️ SSL certificates volume does not exist: $CERTS_VOLUME"
        print_status $BLUE "Creating volume..."
        docker volume create "$CERTS_VOLUME"
        print_status $GREEN "✅ Created SSL certificates volume"
    fi
    
    if docker volume ls | grep -q "$WEBROOT_VOLUME"; then
        print_status $GREEN "✅ Webroot volume exists: $WEBROOT_VOLUME"
    else
        print_status $YELLOW "⚠️ Webroot volume does not exist: $WEBROOT_VOLUME"
        print_status $BLUE "Creating volume..."
        docker volume create "$WEBROOT_VOLUME"
        print_status $GREEN "✅ Created webroot volume"
    fi
}

# Function to check domain resolution
check_domain() {
    print_status $BLUE "🔍 Checking domain resolution..."
    
    if nslookup "$DOMAIN" >/dev/null 2>&1; then
        print_status $GREEN "✅ Domain resolves: $DOMAIN"
        return 0
    else
        print_status $RED "❌ Domain does not resolve: $DOMAIN"
        return 1
    fi
}

# Function to check port accessibility
check_ports() {
    print_status $BLUE "🔍 Checking port accessibility..."
    
    # Check port 80
    if nc -z "$DOMAIN" 80 2>/dev/null; then
        print_status $GREEN "✅ Port 80 is accessible"
    else
        print_status $YELLOW "⚠️ Port 80 is not accessible (this might be OK if nginx is not running)"
    fi
    
    # Check port 443
    if nc -z "$DOMAIN" 443 2>/dev/null; then
        print_status $GREEN "✅ Port 443 is accessible"
    else
        print_status $YELLOW "⚠️ Port 443 is not accessible (this might be OK if nginx is not running)"
    fi
}

# Function to test certbot configuration
test_certbot_config() {
    print_status $BLUE "🔍 Testing certbot configuration..."
    
    # Test certbot help
    if docker run --rm certbot/certbot:latest --help >/dev/null 2>&1; then
        print_status $GREEN "✅ Certbot image is working"
    else
        print_status $RED "❌ Certbot image is not working"
        return 1
    fi
    
    # Test webroot access
    if docker run --rm \
        -v "$WEBROOT_VOLUME:/var/www/certbot" \
        certbot/certbot:latest \
        --version >/dev/null 2>&1; then
        print_status $GREEN "✅ Certbot can access webroot volume"
    else
        print_status $RED "❌ Certbot cannot access webroot volume"
        return 1
    fi
    
    return 0
}

# Function to check existing certificates
check_certificates() {
    print_status $BLUE "🔍 Checking existing certificates..."
    
    if docker run --rm \
        -v "$CERTS_VOLUME:/etc/letsencrypt" \
        certbot/certbot:latest \
        certificates >/dev/null 2>&1; then
        print_status $GREEN "✅ Certbot can access certificates volume"
        
        # Show certificate details
        print_status $BLUE "📋 Certificate details:"
        docker run --rm \
            -v "$CERTS_VOLUME:/etc/letsencrypt" \
            certbot/certbot:latest \
            certificates
    else
        print_status $YELLOW "⚠️ No certificates found or cannot access certificates volume"
    fi
}

# Function to test webroot challenge
test_webroot_challenge() {
    print_status $BLUE "🔍 Testing webroot challenge setup..."
    
    # Create test file in webroot
    docker run --rm \
        -v "$WEBROOT_VOLUME:/var/www/certbot" \
        alpine:latest \
        sh -c "mkdir -p /var/www/certbot/.well-known/acme-challenge && echo 'test' > /var/www/certbot/.well-known/acme-challenge/test.txt"
    
    if [ $? -eq 0 ]; then
        print_status $GREEN "✅ Webroot challenge directory is writable"
        
        # Clean up test file
        docker run --rm \
            -v "$WEBROOT_VOLUME:/var/www/certbot" \
            alpine:latest \
            rm -f /var/www/certbot/.well-known/acme-challenge/test.txt
    else
        print_status $RED "❌ Webroot challenge directory is not writable"
        return 1
    fi
    
    return 0
}

# Function to test entrypoint script
test_entrypoint_script() {
    print_status $BLUE "🔍 Testing entrypoint script..."
    
    if [ -f "scripts/certbot-entrypoint.sh" ]; then
        print_status $GREEN "✅ Entrypoint script exists"
        
        # Test script syntax
        if bash -n scripts/certbot-entrypoint.sh 2>/dev/null; then
            print_status $GREEN "✅ Entrypoint script syntax is valid"
        else
            print_status $RED "❌ Entrypoint script has syntax errors"
            return 1
        fi
    else
        print_status $RED "❌ Entrypoint script not found: scripts/certbot-entrypoint.sh"
        return 1
    fi
    
    return 0
}

# Main function
main() {
    print_status $BLUE "🚀 Starting certbot configuration test"
    print_status $BLUE "🌐 Domain: $DOMAIN"
    print_status $BLUE "📧 Email: $EMAIL"
    echo
    
    local exit_code=0
    
    # Run all checks
    check_docker || exit_code=1
    echo
    
    check_volumes
    echo
    
    check_domain || exit_code=1
    echo
    
    check_ports
    echo
    
    test_certbot_config || exit_code=1
    echo
    
    check_certificates
    echo
    
    test_webroot_challenge || exit_code=1
    echo
    
    test_entrypoint_script || exit_code=1
    echo
    
    # Summary
    if [ $exit_code -eq 0 ]; then
        print_status $GREEN "🎉 All tests passed! Certbot should work correctly."
        print_status $BLUE "💡 Next steps:"
        print_status $BLUE "   1. Run 'docker-compose up -d' to start services"
        print_status $BLUE "   2. Check logs with 'docker-compose logs certbot'"
        print_status $BLUE "   3. If no certificates exist, run './scripts/init-cert.sh' first"
    else
        print_status $RED "❌ Some tests failed. Please fix the issues above before proceeding."
        print_status $BLUE "💡 Common fixes:"
        print_status $BLUE "   - Ensure Docker is running"
        print_status $BLUE "   - Check domain DNS configuration"
        print_status $BLUE "   - Verify ports 80 and 443 are accessible"
        print_status $BLUE "   - Ensure entrypoint script exists and is valid"
    fi
    
    exit $exit_code
}

# Run main function
main "$@" 