# Bina Cloud Server

Servidor Spring Boot para gerenciamento de eventos e notificações em tempo real.

## Funcionalidades

- ✨ API REST para gerenciamento de eventos
- 🔄 Notificações em tempo real via WebSocket
- 📱 Integração com url externa
- 📊 Interface web para monitoramento
- 📝 Histórico de eventos
- 🔒 Banco de dados H2 com persistência em arquivo

## Páginas

- `/monitor` - Monitor em tempo real com WebSocket
  - Exibe eventos em tempo real
  - Abre automaticamente URLs do portal Gas Delivery
  - Mostra status da conexão WebSocket
  - Notificações visuais para novos eventos
  
- `/eventos` - Histórico dos últimos 10 eventos
  - Lista ordenada por data/hora
  - Atualização manual via botão "Atualizar"

- `/swagger-ui.html` - Documentação da API
  - Endpoints disponíveis
  - Modelos de requisição
  - Testes interativos

## Requisitos

- Java 17
- Maven 3.x
- Docker (opcional)

## Executando o Projeto

### Usando Maven

```bash
# Compilar e executar
mvn spring-boot:run
```

### Usando Docker Compose

```bash
# Construir e iniciar containers
docker compose up -d

# Parar containers
docker compose down
```

## Endpoints da API

### POST /api/eventos
Cria um novo evento.

```bash
curl -X POST http://localhost:8080/api/eventos \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Teste de evento",
    "deviceId": "device123",
    "eventType": "TEST",
    "additionalData": "{\"numero\":\"11999999999\",\"data\":\"15/03/2025 17:47:06\"}"
  }'
```

### GET /api/eventos
Lista todos os eventos cadastrados.

```bash
curl http://localhost:8080/api/eventos
```

### GET /api/eventos/{id}
Busca um evento específico por ID.

```bash
curl http://localhost:8080/api/eventos/1
```

## Configuração do Banco de Dados

O projeto usa H2 Database com persistência em arquivo:

- URL: jdbc:h2:file:./data/eventosdb
- Username: sa
- Password: password
- Console: http://localhost:8080/h2-console



## Desenvolvimento

### Estrutura do Projeto

```
src/main/java/com/bina/cloud/
├── config/          # Configurações (WebSocket, CORS)
├── controller/      # Controllers REST e Web
├── model/          # Entidades JPA
├── repository/     # Repositórios Spring Data
└── service/        # Lógica de negócio
```

### Tecnologias Utilizadas

- Spring Boot 3.2.3
- Spring WebSocket
- Spring Data JPA
- H2 Database
- Thymeleaf
- Bootstrap 5
- SockJS
- STOMP WebSocket
- Swagger/OpenAPI

## Monitoramento

- Actuator endpoints: http://localhost:8080/actuator
- Health check: http://localhost:8080/actuator/health
- Métricas: http://localhost:8080/actuator/metrics

## Documentação
- Swagger UI: http://localhost/swagger-ui.html
- OpenAPI: http://localhost/api-docs

## Ambiente de Produção

Para ambiente de produção, configure as seguintes variáveis de ambiente:

```properties
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
JAVA_OPTS=-Xmx256m -Xms128m
```

## Manutenção
### Logs
- Aplicação: `docker compose logs app`
- Nginx: `docker compose logs nginx`

### Backup
1. Dados H2: Backup do volume `h2-data`
2. Configurações: Backup dos arquivos de configuração

### Atualização
1. Pull das últimas alterações: `git pull`
2. Reconstruir containers: `docker compose up -d --build`



### Debug
1. Logs detalhados:
   ```bash
   docker compose logs -f app
   ```

2. Acesso ao container:
   ```bash
   docker compose exec app sh
   ```

3. Verificar status:
   ```bash
   docker compose ps
   ```


## Backup e Restauração

### Backup
```bash
docker compose exec -T app tar czf - /data > app-backup.tar.gz
```

### Restauração
```bash
docker compose exec -T app tar xzf - < app-backup.tar.gz
```

## Troubleshooting

### Problemas de Memória
1. Verifique o uso de memória:
```bash
docker stats
```

2. Se necessário, ajuste os limites:
```bash
docker compose down
# Edite os limites de memória no docker-compose.yml
docker compose up -d
```

3. Monitore os logs:
```bash
docker compose logs -f
```

### Problemas de Certificado SSL
1. Renove manualmente:
```bash
docker compose exec nginx certbot renew
```

2. Verifique os logs:
```bash
docker compose logs nginx
```


## Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.
