#!/bin/bash

DOMAIN="bina.fernandojunior.com.br"
EMAIL="fernando.medeiros@gmail.com"

echo "🔍 Diagnóstico de Conectividade - Bina Cloud Server"
echo "=================================================="
echo "Domínio: $DOMAIN"
echo "Email: $EMAIL"
echo

# 1. Verificar DNS
echo "1️⃣ Verificando DNS..."
echo "   Resolução local:"
nslookup "$DOMAIN" 2>/dev/null | grep -E "(Name:|Address:)"
echo

# 2. Verificar conectividade externa
echo "2️⃣ Verificando conectividade externa..."
echo "   Testando HTTP (porta 80):"
if curl -s -m 10 -I "http://$DOMAIN" > /dev/null 2>&1; then
    echo "   ✅ HTTP acessível"
    curl -s -m 5 -I "http://$DOMAIN" | head -5
else
    echo "   ❌ HTTP não acessível"
fi
echo

# 3. Verificar se o servidor está respondendo
echo "3️⃣ Verificando resposta do servidor..."
echo "   IP do servidor:"
curl -s -m 5 "http://$DOMAIN" 2>/dev/null | head -3
echo

# 4. Verificar portas locais
echo "4️⃣ Verificando portas locais..."
echo "   Porta 80:"
if ss -tuln | grep -q ":80 "; then
    echo "   ✅ Porta 80 em uso"
    ss -tuln | grep ":80 "
else
    echo "   ❌ Porta 80 não está em uso"
fi

echo "   Porta 443:"
if ss -tuln | grep -q ":443 "; then
    echo "   ✅ Porta 443 em uso"
    ss -tuln | grep ":443 "
else
    echo "   ❌ Porta 443 não está em uso"
fi
echo

# 5. Verificar containers Docker
echo "5️⃣ Verificando containers Docker..."
if command -v docker &> /dev/null; then
    echo "   Containers ativos:"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | head -10
else
    echo "   ❌ Docker não encontrado"
fi
echo

# 6. Verificar volumes Docker
echo "6️⃣ Verificando volumes Docker..."
if command -v docker &> /dev/null; then
    echo "   Volumes:"
    docker volume ls | grep -E "(ssl-certs|certbot-web|h2-data)"
else
    echo "   ❌ Docker não encontrado"
fi
echo

# 7. Teste de conectividade externa
echo "7️⃣ Teste de conectividade externa..."
echo "   Testando de fora (usando curl externo):"
EXTERNAL_IP=$(curl -s -m 5 ifconfig.me 2>/dev/null || echo "Não foi possível obter IP externo")
echo "   IP externo: $EXTERNAL_IP"

# Testar se o domínio resolve para o IP correto
DOMAIN_IP=$(nslookup "$DOMAIN" 2>/dev/null | grep "Address:" | tail -1 | awk '{print $2}')
echo "   IP do domínio: $DOMAIN_IP"

if [ "$EXTERNAL_IP" != "Não foi possível obter IP externo" ] && [ "$DOMAIN_IP" != "" ]; then
    if [ "$EXTERNAL_IP" = "$DOMAIN_IP" ]; then
        echo "   ✅ DNS apontando para IP correto"
    else
        echo "   ⚠️ DNS pode não estar apontando para IP correto"
        echo "   💡 Configure o DNS para apontar $DOMAIN para $EXTERNAL_IP"
    fi
else
    echo "   ❌ Não foi possível verificar DNS"
fi
echo

# 8. Teste de firewall
echo "8️⃣ Teste de firewall..."
echo "   Verificando se portas estão abertas:"
if command -v ufw &> /dev/null; then
    echo "   Status UFW:"
    ufw status | head -5
else
    echo "   UFW não encontrado"
fi

if command -v iptables &> /dev/null; then
    echo "   Regras iptables (porta 80):"
    iptables -L -n | grep -E "(80|443)" | head -3
fi
echo

# 9. Recomendações
echo "9️⃣ Recomendações:"
echo "   📋 Checklist para resolver problemas:"
echo "   □ DNS apontando para IP correto ($EXTERNAL_IP)"
echo "   □ Portas 80 e 443 liberadas no firewall"
echo "   □ Nginx rodando e acessível"
echo "   □ Domínio acessível externamente"
echo "   □ Certbot com permissões corretas"
echo

# 10. Comandos úteis
echo "🔧 Comandos úteis:"
echo "   Ver logs nginx: docker-compose logs nginx"
echo "   Ver logs certbot: docker run --rm -v ssl-certs:/etc/letsencrypt alpine cat /etc/letsencrypt/letsencrypt.log"
echo "   Testar nginx: curl -I http://$DOMAIN"
echo "   Verificar certificado: docker run --rm -v ssl-certs:/etc/letsencrypt alpine ls -la /etc/letsencrypt/live/"
echo "   Reiniciar nginx: docker-compose restart nginx"
echo

echo "✅ Diagnóstico concluído!"

echo "🔍 Diagnóstico de Memória - Bina Cloud Server"
echo "=============================================="

# Verificar se o container está rodando
if ! docker ps | grep -q "bina-cloud-server_app_1"; then
    echo "❌ Container da aplicação não está rodando!"
    exit 1
fi

echo "📊 Status dos Containers:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "💾 Uso de Memória dos Containers:"
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"

echo ""
echo "🔧 Configurações JVM da Aplicação:"
docker exec bina-cloud-server_app_1 java -XX:+PrintFlagsFinal -version 2>/dev/null | grep -E "(MaxHeapSize|MaxMetaspaceSize|CompressedClassSpaceSize)" || echo "Não foi possível obter configurações JVM"

echo ""
echo "📈 Logs de GC (últimas 20 linhas):"
if docker exec bina-cloud-server_app_1 test -f /tmp/gc.log; then
    docker exec bina-cloud-server_app_1 tail -20 /tmp/gc.log 2>/dev/null || echo "Arquivo de GC não encontrado"
else
    echo "Arquivo de GC não encontrado"
fi

echo ""
echo "🗑️ Heap Dumps (se existirem):"
docker exec bina-cloud-server_app_1 ls -la /tmp/*.hprof 2>/dev/null || echo "Nenhum heap dump encontrado"

echo ""
echo "📋 Logs da Aplicação (últimas 10 linhas com erro):"
docker logs --tail 10 bina-cloud-server_app_1 2>&1 | grep -i "error\|exception\|outofmemory" || echo "Nenhum erro encontrado nos logs recentes"

echo ""
echo "🌐 Status da Aplicação:"
if curl -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo "✅ Aplicação respondendo na porta 8080"
    curl -s http://localhost:8080/actuator/health | jq . 2>/dev/null || curl -s http://localhost:8080/actuator/health
else
    echo "❌ Aplicação não está respondendo na porta 8080"
fi

echo ""
echo "🔍 Verificações Adicionais:"
echo "1. Verificar se há muitos eventos no banco:"
docker exec bina-cloud-server_app_1 wget -qO- http://localhost:8080/h2-console 2>/dev/null | grep -q "H2 Console" && echo "   ✅ H2 Console acessível" || echo "   ❌ H2 Console não acessível"

echo "2. Verificar uso de disco:"
docker exec bina-cloud-server_app_1 df -h /app/data 2>/dev/null || echo "   ❌ Não foi possível verificar uso de disco"

echo ""
echo "💡 Recomendações:"
echo "- Se o Metaspace estiver alto, considere aumentar MaxMetaspaceSize"
echo "- Se o heap estiver alto, verifique se há memory leaks no código"
echo "- Monitore os logs de GC para identificar padrões de uso de memória"
echo "- Considere reduzir o número de conexões simultâneas se necessário" 