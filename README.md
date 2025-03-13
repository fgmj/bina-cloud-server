# Bina Cloud Server

## Descrição
Servidor backend Spring Boot para notificações de eventos Android.

## Requisitos
- Java 17
- Maven
- Docker e Docker Compose V2
- Domínio público (para HTTPS em produção)

## Requisitos Mínimos

- Docker e Docker Compose
- 1GB de RAM
- 1 vCPU
- 10GB de espaço em disco

## Como Usar
1. Clone o repositório
2. Configure as variáveis de ambiente:
   ```bash
   cp .env.template .env
   # Edite o arquivo .env com suas configurações
   ```

3. Build e execução:
   ```bash
   # Usando Docker BuildKit (recomendado)
   docker buildx bake --load

   # Ou usando Docker Compose diretamente
   docker compose up -d --build
   ```

4. Acesse:
   - API: https://seu-dominio.com (via Nginx com HTTPS)
   - Swagger UI: https://seu-dominio.com/swagger-ui.html
   - H2 Console: https://seu-dominio.com/h2-console   
   
## Arquitetura
O projeto utiliza uma arquitetura em camadas:
- Controller: Endpoints REST
- Service: Lógica de negócios
- Repository: Acesso a dados
- Entity: Modelos de dados

## Configuração
### Banco de Dados
O projeto usa H2 como banco de dados:
- Banco de dados em arquivo para persistência
- Console web para administração
- Baixo consumo de recursos
- Ideal para aplicações de pequeno/médio porte

### Nginx
O projeto inclui um proxy reverso Nginx que:
- Expõe a porta 80 para acesso externo
- Redireciona requisições para a aplicação Spring Boot
- Configura headers de segurança
- Gerencia timeouts e conexões
- Suporta WebSocket para H2 Console


## Segurança
- Headers de segurança configurados
- Proteção contra ataques comuns
- Validação de entrada

## Documentação
- Swagger UI: http://localhost/swagger-ui.html
- OpenAPI: http://localhost/api-docs

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

## Troubleshooting
### Problemas Comuns
1. Porta 80 em uso:
   - Verifique se há outro serviço usando a porta
   - Altere a porta no docker-compose.yml

2. Erro de conexão com banco:
   - Verifique as credenciais no .env
   - Confirme se o banco está acessível

3. Nginx não redireciona:
   - Verifique os logs: `docker compose logs nginx`
   - Confirme se a aplicação está rodando

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

## Features

- REST API endpoint (`/api/eventos`) for event data
- Built-in H2 database with persistent storage
- Event logging and persistence
- Scalable architecture
- Docker-based deployment


## Database Configuration
The application uses H2 database:
- File-based with persistence
- Database location: `./data/eventosdb` (Docker: mounted as volume)
- H2 Console: http://localhost:8080/h2-console
- Default credentials:
  ```properties
  JDBC URL: jdbc:h2:file:./data/eventosdb
  Username: sa
  Password: password

## Otimizações de Memória

O sistema foi otimizado para rodar em ambientes com recursos limitados (1GB RAM):

### Spring Boot
- Heap size limitado a 256MB
- Connection pool reduzido para 3 conexões
- Cache Caffeine otimizado
- Logging reduzido
- Batch size reduzido para 20

### Nginx
- Worker processes limitado a 1
- Worker connections reduzido para 512
- Buffer sizes otimizados
- Access log desabilitado
- File cache otimizado



## Variáveis de Ambiente

Configure as seguintes variáveis no arquivo `.env`:

```env
NGINX_HOST=seu-dominio.com
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

## Suporte

Para suporte, abra uma issue no GitHub ou envie um email para suporte@example.com.

## Configuração HTTPS
### Desenvolvimento
- Certificados auto-assinados são gerados automaticamente
- Acesse via https://localhost
- Ignore os avisos de certificado no navegador

### Produção
1. Configure as variáveis de ambiente:
   ```bash
   DOMAIN=seu-dominio.com
   ENVIRONMENT=prod
   CERTBOT_EMAIL=seu-email@exemplo.com
   ```

2. Certifique-se que:
   - O domínio aponta para o IP do servidor
   - As portas 80 e 443 estão liberadas no firewall
   - O servidor tem acesso à internet

3. Os certificados SSL serão:
   - Gerados automaticamente na primeira execução
   - Renovados automaticamente antes de expirarem
   - Armazenados em um volume Docker persistente

### Troubleshooting HTTPS
1. Certificados não são gerados:
   - Verifique se o domínio está configurado corretamente
   - Confirme se as portas 80 e 443 estão acessíveis
   - Verifique os logs: `docker compose logs nginx`

2. Erro de certificado:
   - Em desenvolvimento: Use http://localhost:8080
   - Em produção: Verifique se o domínio está correto
   - Confirme se os certificados foram gerados

3. Redirecionamento não funciona:
   - Verifique a configuração do Nginx
   - Confirme se o SSL está habilitado
   - Verifique os logs do Nginx

# ... rest of the existing content ... 