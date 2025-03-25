# Função para enviar um evento com log detalhado
function Send-Event {
    param (
        [string]$deviceId,
        [string]$eventType,
        [string]$phoneNumber,
        [string]$timestamp,
        [string]$additionalData,
        [string]$description,
        [string]$tipoEvento,
        [string]$numeroOrigem,
        [string]$numeroDestino,
        [int]$duracaoSegundos
    )

    $body = @{
        deviceId = $deviceId
        eventType = $eventType
        phoneNumber = $phoneNumber
        timestamp = $timestamp  # Voltando para timestamp pois é o nome correto na entidade
        additionalData = $additionalData
        description = $description
        tipoEvento = $tipoEvento
        numeroOrigem = $numeroOrigem
        numeroDestino = $numeroDestino
        duracaoSegundos = $duracaoSegundos
    } | ConvertTo-Json

    Write-Host "Enviando evento para: http://localhost:8080/api/eventos"
    Write-Host "Body: $body"

    try {
        # Primeiro, fazer login para obter o token de sessão
        $loginBody = @{
            username = "admin"
            password = "admin"
        } | ConvertTo-Json

        Write-Host "Fazendo login..."
        $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/login" -Method Post -Body $loginBody -ContentType "application/json" -SessionVariable session
        
        # Agora enviar o evento usando a mesma sessão
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/eventos" -Method Post -Body $body -ContentType "application/json" -WebSession $session
        Write-Host "Resposta recebida: $response"
    }
    catch {
        Write-Host "Erro ao enviar evento: $_"
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)"
        Write-Host "Status Description: $($_.Exception.Response.StatusDescription)"
    }
}

# Enviar um único evento para teste
$timestamp = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
Write-Host "Iniciando envio de evento de teste..."
Write-Host "Timestamp: $timestamp"

Send-Event -deviceId "test-device-001" `
          -eventType "ANSWERED" `
          -phoneNumber "5511999999999" `
          -timestamp $timestamp `
          -additionalData '{"duration": "120", "caller": "5511999999999", "called": "5511988888888"}' `
          -description "Chamada recebida e atendida" `
          -tipoEvento "INCOMING_CALL" `
          -numeroOrigem "5511999999999" `
          -numeroDestino "5511988888888" `
          -duracaoSegundos 120

Write-Host "Teste concluído."