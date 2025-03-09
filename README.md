# Bina Cloud Server

A Spring Boot-based backend designed to receive event notifications from Android applications.

## Features

- REST API endpoint (`/api/eventos`) for event data
- Multiple database support (H2 for development, Oracle for production)
- Event logging and persistence
- Scalable architecture

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Oracle Database (for production only)

## Database Configuration

The application supports different database configurations based on the environment:

### Development (Default)
Uses H2 file-based database for local development:
- Database file location: `./data/eventosdb`
- H2 Console: http://localhost:8080/h2-console
- Credentials:
  ```properties
  JDBC URL: jdbc:h2:file:./data/eventosdb
  Username: sa
  Password: password
  ```

### Testing
Uses H2 in-memory database:
- Automatically configured for tests
- No persistence between test runs
- Clean database for each test execution

### Production
Uses Oracle Database:
- Requires Oracle database instance
- Configure connection in `application-prod.properties`
- Set environment variable `ORACLE_PASSWORD` for database password

## Building and Running

### Development Mode (Default)
```bash
# Run with H2 database
mvn spring-boot:run
```

### Production Mode
```bash
# Set Oracle password
export ORACLE_PASSWORD=your_secure_password

# Run with Oracle database
mvn spring-boot:run -Dspring.profiles.active=prod
```

### Building for Production
```bash
# Build the application
mvn clean package

# Run the JAR file
java -jar target/bina-cloud-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Database Setup

### Development (H2)
- No setup required
- Database and schema are automatically created
- Data persisted in `./data/eventosdb` file

### Production (Oracle)
1. Create the database user:
   ```sql
   CREATE USER bina_cloud IDENTIFIED BY your_password;
   GRANT CONNECT, RESOURCE TO bina_cloud;
   GRANT CREATE SESSION TO bina_cloud;
   GRANT UNLIMITED TABLESPACE TO bina_cloud;
   ```

2. Update Oracle connection details in `application-prod.properties`:
   ```properties
   spring.datasource.url=jdbc:oracle:thin:@//your-oracle-host:1521/your-service
   spring.datasource.username=bina_cloud
   ```

## API Endpoints

- `POST /api/eventos`: Create a new event
- `GET /api/eventos`: Retrieve all events
- `GET /api/eventos/{id}`: Retrieve a specific event by ID

### Request Example

```json
POST /api/eventos
{
    "description": "User login event",
    "deviceId": "android-device-001",
    "eventType": "LOGIN",
    "additionalData": "{\"location\": \"São Paulo\", \"status\": \"success\"}"
}
```

## Development Tools

### H2 Console
- Available in development mode at: http://localhost:8080/h2-console
- Useful for database inspection and debugging
- Disabled in production for security

### Logging
- Development: Detailed SQL logging enabled
- Production: Minimal logging for better performance

## Planned Enhancements

- [ ] Add authentication and authorization
- [ ] Implement configuration endpoint
- [ ] Deploy to Oracle Cloud
- [ ] Add monitoring and metrics

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Security Notes

- Never commit sensitive information (passwords, API keys)
- Use environment variables for sensitive data
- The H2 console is disabled in production
- Oracle credentials should be managed securely in production

## Deployment to Oracle Cloud (Ubuntu 24.04)

### Option 1: Docker Deployment (Recommended)

1. Connect to your Oracle Cloud instance:
   ```bash
   ssh ubuntu@your-instance-ip
   ```

2. Install Docker and Docker Compose:
   ```bash
   # Update system
   sudo apt update && sudo apt upgrade -y

   # Install required packages
   sudo apt install -y apt-transport-https ca-certificates curl software-properties-common

   # Add Docker's official GPG key
   curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

   # Add Docker repository
   echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

   # Install Docker
   sudo apt update
   sudo apt install -y docker-ce docker-ce-cli containerd.io

   # Add your user to docker group
   sudo usermod -aG docker $USER
   newgrp docker
   ```

3. Create a Dockerfile in your project root:
   ```dockerfile
   FROM eclipse-temurin:17-jre-jammy

   WORKDIR /app
   COPY target/bina-cloud-server-0.0.1-SNAPSHOT.jar app.jar

   EXPOSE 8080
   ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
   ```

4. Create docker-compose.yml:
   ```yaml
   version: '3.8'
   services:
     app:
       build: .
       ports:
         - "8080:8080"
       environment:
         - ORACLE_PASSWORD=${ORACLE_PASSWORD}
       restart: unless-stopped
       networks:
         - app-network

   networks:
     app-network:
       driver: bridge
   ```

5. Deploy the application:
   ```bash
   # Clone the repository
   git clone your-repository-url
   cd bina-cloud-server

   # Build the application
   ./mvnw clean package -DskipTests

   # Create .env file
   echo "ORACLE_PASSWORD=your_secure_password" > .env

   # Build and run with Docker Compose
   docker compose up -d --build
   ```

### Option 2: Direct JAR Deployment

1. Install Java 17:
   ```bash
   # Update system
   sudo apt update && sudo apt upgrade -y

   # Install Java
   sudo apt install -y openjdk-17-jdk

   # Verify installation
   java -version
   ```

2. Create a dedicated user:
   ```bash
   sudo useradd -r -m -U -d /opt/bina-cloud -s /bin/bash binacloud
   sudo usermod -aG sudo binacloud
   ```

3. Set up the application:
   ```bash
   # Switch to application user
   sudo su - binacloud

   # Create application directory
   mkdir -p /opt/bina-cloud/app

   # Copy the JAR file
   cp bina-cloud-server-0.0.1-SNAPSHOT.jar /opt/bina-cloud/app/

   # Create application.properties
   cp src/main/resources/application-prod.properties /opt/bina-cloud/app/application.properties
   ```

4. Create a systemd service:
   ```bash
   sudo nano /etc/systemd/system/bina-cloud.service
   ```
   Add the following content:
   ```ini
   [Unit]
   Description=Bina Cloud Server
   After=network.target

   [Service]
   User=binacloud
   Environment="ORACLE_PASSWORD=your_secure_password"
   WorkingDirectory=/opt/bina-cloud/app
   ExecStart=/usr/bin/java -jar bina-cloud-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
   SuccessExitStatus=143
   TimeoutStopSec=10
   Restart=on-failure
   RestartSec=5

   [Install]
   WantedBy=multi-user.target
   ```

5. Start the service:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable bina-cloud
   sudo systemctl start bina-cloud
   ```

### Security Configuration

1. Configure firewall:
   ```bash
   # Install UFW if not present
   sudo apt install -y ufw

   # Configure firewall rules
   sudo ufw allow ssh
   sudo ufw allow 8080/tcp
   sudo ufw enable
   ```

2. Set up SSL/TLS (recommended):
   ```bash
   # Install Certbot
   sudo apt install -y certbot python3-certbot-nginx

   # Install and configure Nginx
   sudo apt install -y nginx
   
   # Get SSL certificate
   sudo certbot --nginx -d your-domain.com
   ```

3. Configure Nginx as reverse proxy:
   ```nginx
   server {
       listen 443 ssl;
       server_name your-domain.com;

       ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
       ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

       location / {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
   }
   ```

### Monitoring

1. Install monitoring tools:
   ```bash
   # Install monitoring basics
   sudo apt install -y htop net-tools

   # For log monitoring
   sudo apt install -y logwatch
   ```

2. View application logs:
   ```bash
   # Docker deployment
   docker compose logs -f app

   # Direct deployment
   sudo journalctl -u bina-cloud -f
   ```

### Backup Strategy

1. Create backup script:
   ```bash
   sudo nano /opt/bina-cloud/backup.sh
   ```
   ```bash
   #!/bin/bash
   BACKUP_DIR="/opt/bina-cloud/backups"
   TIMESTAMP=$(date +%Y%m%d_%H%M%S)
   
   # Create backup directory
   mkdir -p $BACKUP_DIR
   
   # Backup application files
   tar -czf $BACKUP_DIR/app_$TIMESTAMP.tar.gz /opt/bina-cloud/app/
   
   # Keep only last 7 days of backups
   find $BACKUP_DIR -type f -mtime +7 -delete
   ```

2. Set up automatic backups:
   ```bash
   sudo chmod +x /opt/bina-cloud/backup.sh
   
   # Add to crontab
   (crontab -l 2>/dev/null; echo "0 2 * * * /opt/bina-cloud/backup.sh") | crontab -
   ``` 