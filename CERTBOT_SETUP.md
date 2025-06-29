# Certbot Setup and Troubleshooting Guide

This guide explains how to set up and troubleshoot Let's Encrypt SSL certificates using Certbot in the Bina Cloud Server.

## Overview

The setup uses:
- **Certbot** for automatic SSL certificate management
- **Webroot challenge** for domain validation
- **Nginx** as the web server
- **Docker Compose** for orchestration

## Architecture

```
Internet → Nginx (Port 80/443) → App (Port 8080)
                ↓
            Certbot (Automatic renewal)
                ↓
            Let's Encrypt (SSL certificates)
```

## Quick Start

### 1. Initial Setup

```bash
# Create required volumes
docker volume create ssl-certs
docker volume create certbot-web

# Test the configuration
./scripts/test-certbot.sh

# Start services
docker-compose up -d
```

### 2. Initial Certificate Generation

If no certificates exist, run:

```bash
./scripts/init-cert.sh
```

### 3. Verify Setup

```bash
# Check service status
docker-compose ps

# View certbot logs
docker-compose logs certbot

# Check certificate status
docker-compose exec certbot certbot certificates
```

## Configuration Files

### Docker Compose (`docker-compose.yml`)

The certbot service is configured with:
- **Image**: `certbot/certbot:latest`
- **Volumes**: 
  - `ssl-certs` → `/etc/letsencrypt` (certificates)
  - `certbot-web` → `/var/www/certbot` (webroot)
  - `certbot-logs` → `/var/log/letsencrypt` (logs)
  - `./scripts/certbot-entrypoint.sh` → `/entrypoint.sh` (entrypoint script)
- **Environment Variables**:
  - `CERTBOT_EMAIL`: Email for Let's Encrypt notifications
  - `DOMAIN`: Domain name for certificates
  - `RENEWAL_INTERVAL`: How often to check for renewal (default: 12h)

### Nginx Configuration (`nginx/nginx.conf`)

The nginx configuration includes:
- **HTTP server** (port 80): Handles Let's Encrypt challenges
- **HTTPS server** (port 443): Serves the application with SSL
- **Webroot challenge location**: `/.well-known/acme-challenge/`

### Entrypoint Script (`scripts/certbot-entrypoint.sh`)

The entrypoint script handles:
- Initial certificate generation (if none exist)
- Automatic renewal every 12 hours
- Certificate expiry checking
- Nginx configuration reload after renewal
- Proper error handling and logging

## Troubleshooting

### Common Issues

#### 1. "Unable to open config file" Error

**Problem**: Certbot is trying to read a shell script as a config file.

**Solution**: Use the provided entrypoint script instead of embedding commands in docker-compose.

#### 2. Certificate Not Found

**Problem**: No SSL certificates exist for the domain.

**Solution**: 
```bash
# Run initial certificate generation
./scripts/init-cert.sh

# Or manually:
docker run --rm \
  -v "ssl-certs:/etc/letsencrypt" \
  -v "certbot-web:/var/www/certbot" \
  certbot/certbot certonly \
  --webroot \
  --webroot-path=/var/www/certbot \
  --email "your-email@example.com" \
  --agree-tos \
  --no-eff-email \
  -d "your-domain.com"
```

#### 3. Domain Validation Fails

**Problem**: Let's Encrypt cannot validate domain ownership.

**Solutions**:
- Ensure domain DNS points to your server
- Verify ports 80 and 443 are accessible
- Check firewall settings
- Ensure nginx is running and accessible

#### 4. Nginx Reload Fails

**Problem**: Certbot cannot reload nginx after certificate renewal.

**Solution**: The entrypoint script handles this gracefully and logs the issue.

### Diagnostic Commands

#### Test Configuration
```bash
./scripts/test-certbot.sh
```

#### Check Certificate Status
```bash
# View existing certificates
docker-compose exec certbot certbot certificates

# Check certificate expiry
docker-compose exec certbot openssl x509 -enddate -noout -in /etc/letsencrypt/live/bina.fernandojunior.com.br/fullchain.pem
```

#### View Logs
```bash
# Certbot logs
docker-compose logs certbot

# Nginx logs
docker-compose logs nginx

# All services
docker-compose logs
```

#### Manual Renewal
```bash
# Force certificate renewal
docker-compose exec certbot certbot renew --force-renewal

# Test renewal (dry run)
docker-compose exec certbot certbot renew --dry-run
```

#### Check Webroot Access
```bash
# Test webroot challenge directory
curl -I http://your-domain.com/.well-known/acme-challenge/test.txt
```

## Environment Variables

Set these in your environment or `.env` file:

```bash
# Required
CERTBOT_EMAIL=your-email@example.com
NGINX_HOST=your-domain.com

# Optional
RENEWAL_INTERVAL=12h  # How often to check for renewal
```

## Security Considerations

1. **Email Address**: Use a real email address for Let's Encrypt notifications
2. **Domain Ownership**: Ensure you own the domain and have DNS control
3. **Firewall**: Allow ports 80 and 443 for Let's Encrypt validation
4. **Backup**: Certificates are stored in Docker volumes - backup if needed
5. **Monitoring**: Monitor certificate expiry and renewal success

## Maintenance

### Regular Tasks

1. **Monitor Logs**: Check certbot logs regularly for renewal issues
2. **Update Certbot**: Keep the certbot image updated
3. **Test Renewal**: Periodically test certificate renewal with `--dry-run`
4. **Backup Certificates**: Backup the `ssl-certs` volume if needed

### Updating Configuration

When updating the configuration:

1. Stop services: `docker-compose down`
2. Update configuration files
3. Test configuration: `./scripts/test-certbot.sh`
4. Start services: `docker-compose up -d`
5. Monitor logs: `docker-compose logs certbot`

## Scripts Reference

### `scripts/init-cert.sh`
Initial certificate generation for new domains.

### `scripts/renew-cert.sh`
Manual certificate renewal with retry logic.

### `scripts/test-certbot.sh`
Comprehensive testing of the certbot setup.

### `scripts/certbot-entrypoint.sh`
Entrypoint script for the certbot container (automatic renewal).

### `scripts/diagnose.sh`
General system diagnostics.

## Support

If you encounter issues:

1. Run `./scripts/test-certbot.sh` for diagnostics
2. Check logs with `docker-compose logs certbot`
3. Verify domain DNS and accessibility
4. Ensure all required volumes exist
5. Check firewall and port accessibility

## References

- [Let's Encrypt Documentation](https://letsencrypt.org/docs/)
- [Certbot Documentation](https://certbot.eff.org/docs/)
- [Nginx SSL Configuration](https://nginx.org/en/docs/http/configuring_https_servers.html) 