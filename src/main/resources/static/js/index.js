let eventCount = 0;
const maxEvents = 50; // Limite de eventos mostrados

// Variáveis globais
let deviceChart = null;

// Configuração do gráfico de dispositivos
const deviceChartConfig = {
    type: 'doughnut',
    data: {
        labels: ['Ativos', 'Inativos'],
        datasets: [{
            data: [0, 0],
            backgroundColor: ['#28a745', '#dc3545'],
            borderWidth: 0
        }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'bottom'
            }
        }
    }
};

// Inicialização quando o documento estiver pronto
document.addEventListener('DOMContentLoaded', function () {
    console.log('[Dashboard] Initializing dashboard...');

    try {
        // Conectar ao WebSocket
        connect();

        // Carregar dados iniciais
        loadDashboardData();

        // Configurar atualização automática a cada 30 segundos
        setInterval(loadDashboardData, 30000);
    } catch (error) {
        console.error('[Dashboard] Error during initialization:', error);
    }
});

// Conectar ao WebSocket
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

// Configurar logs do STOMP para debug
stompClient.debug = function (str) {
    console.log('[Stomp Debug]', str);
};

function connect() {
    console.log('[WebSocket] Attempting to connect...');
    updateConnectionStatus(true);

    stompClient.connect({},
        function (frame) {
            console.log('[WebSocket] Connected:', frame);
            updateConnectionStatus(true);

            stompClient.subscribe('/topic/events', function (message) {
                console.log('[WebSocket] Received message:', message);
                try {
                    const event = JSON.parse(message.body);
                    console.log('[WebSocket] Parsed event:', event);
                    handleNewEvent(event);
                } catch (error) {
                    console.error('[WebSocket] Error processing message:', error);
                    console.log('[WebSocket] Message body:', message.body);
                }
            });
        },
        function (error) {
            console.error('[WebSocket] Connection error:', error);
            updateConnectionStatus(false);
            // Tentar reconectar após 5 segundos
            setTimeout(connect, 5000);
        }
    );
}

// Função para atualizar status da conexão
function updateConnectionStatus(connected) {
    const indicator = document.getElementById('connectionIndicator');
    const text = document.getElementById('connectionText');
    const lastUpdate = document.getElementById('lastUpdate');

    if (connected) {
        indicator.className = 'connection-indicator connection-connected';
        text.textContent = 'Conectado';
        lastUpdate.textContent = new Date().toLocaleTimeString();
    } else {
        indicator.className = 'connection-indicator connection-disconnected';
        text.textContent = 'Desconectado';
    }
}

function updateConnectionStatusConnecting() {
    const indicator = document.getElementById('connectionIndicator');
    const text = document.getElementById('connectionText');

    indicator.className = 'connection-indicator connection-connecting';
    text.textContent = 'Conectando...';
}

// Função para carregar dados do dashboard
function loadDashboardData() {
    console.log('[Dashboard] Loading dashboard data...');
    fetch('/api/dashboard/stats?period=today')
        .then(response => {
            console.log('[Dashboard] Response status:', response.status);
            return response.json();
        })
        .then(data => {
            console.log('[Dashboard] Data received:', data);
            updateDashboardStats(data);
            updateDevicesList(data.activeDevices);
        })
        .catch(error => console.error('[Dashboard] Error loading data:', error));
}

// Função para atualizar estatísticas do dashboard
function updateDashboardStats(data) {
    const elements = {
        totalCalls: document.getElementById('totalCalls'),
        answeredCalls: document.getElementById('answeredCalls'),
        missedCalls: document.getElementById('missedCalls'),
        busyCalls: document.getElementById('busyCalls'),
        answerRate: document.getElementById('answerRate')
    };

    if (elements.totalCalls) elements.totalCalls.textContent = data.totalCalls || 0;
    if (elements.answeredCalls) elements.answeredCalls.textContent = data.answeredCalls || 0;
    if (elements.missedCalls) elements.missedCalls.textContent = data.missedCalls || 0;
    if (elements.busyCalls) elements.busyCalls.textContent = data.busyCalls || 0;
    if (elements.answerRate) elements.answerRate.textContent = `${(data.answerRate || 0).toFixed(1)}%`;
}

// Função para atualizar lista de dispositivos
function updateDevicesList(devices) {
    console.log('[Dashboard] Updating devices list:', devices);
    const devicesList = document.getElementById('devicesList');
    if (!devicesList) {
        console.warn('[Dashboard] Could not find devicesList element');
        return;
    }

    devicesList.innerHTML = devices.map(device => `
        <div class="device-item ${device.active ? 'active' : 'inactive'}">
            <div class="device-header">
                <div class="device-name">${device.deviceId}</div>
                <div class="device-status ${device.active ? 'active' : 'inactive'}">
                    ${device.active ? 'Ativo' : 'Inativo'}
                </div>
            </div>
            <div class="device-stats">
                <div class="stat-item">
                    <span class="stat-label">Total</span>
                    <span class="stat-value">${device.totalCalls}</span>
                </div>
                <div class="stat-item">
                    <span class="stat-label">Atendidas</span>
                    <span class="stat-value">${device.answeredCalls}</span>
                </div>
                <div class="stat-item">
                    <span class="stat-label">Perdidas</span>
                    <span class="stat-value">${device.missedCalls}</span>
                </div>
                <div class="stat-item">
                    <span class="stat-label">Taxa</span>
                    <span class="stat-value">${device.answerRate.toFixed(1)}%</span>
                </div>
            </div>
            <div class="last-activity">
                Última atividade: ${device.lastActivity}
            </div>
        </div>
    `).join('');
}

// Função para atualizar lista de chamadas recentes
function updateRecentCalls(calls) {
    const recentCallsList = document.getElementById('recentCallsList');
    if (recentCallsList && Array.isArray(calls)) {
        recentCallsList.innerHTML = calls.map(call => `
            <div class="call-item">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <strong>${call.phoneNumber || 'Número não disponível'}</strong>
                        <br>
                        <small class="text-muted">${moment(call.timestamp).format('DD/MM/YYYY HH:mm:ss')}</small>
                    </div>
                    <span class="badge ${getStatusBadgeClass(call.status)}">${getStatusText(call.status)}</span>
                </div>
            </div>
        `).join('');
    }
}

// Função para atualizar tabela de monitoramento
function updateMonitorTable(calls) {
    const monitorTable = document.getElementById('monitorTable');
    if (monitorTable && Array.isArray(calls)) {
        monitorTable.innerHTML = calls.map(call => `
            <tr>
                <td>${call.phoneNumber || 'Número não disponível'}</td>
                <td>${moment(call.timestamp).format('DD/MM/YYYY HH:mm:ss')}</td>
                <td><span class="badge ${getStatusBadgeClass(call.status)}">${getStatusText(call.status)}</span></td>
                <td>${call.device || 'N/A'}</td>
            </tr>
        `).join('');
    }
}

// Função para lidar com novos eventos
function handleNewEvent(event) {
    console.log('[WebSocket] Processing message:', event);
    eventCount++;

    try {
        // O evento pode vir diretamente ou dentro de message.event
        const eventParsed = event.event || event;
        console.log('[WebSocket] Parsed event:', eventParsed);

        // Atualizar dados do dashboard
        loadDashboardData();

        // Atualizar a tabela de monitoramento imediatamente
        if (eventParsed.phoneNumber) {
            if (eventParsed.additionalData) {
                try {
                    const additionalData = JSON.parse(event.additionalData);
                    if (additionalData.numero) {
                        event.url = `https://portal.gasdelivery.com.br/secure/client/?primary_phone=${additionalData.numero}`;
                        // Abrir URL em nova aba
                        chrome.tabs.create({ url: event.url });
                    }
                } catch (error) {
                    console.warn('Erro ao processar additionalData', error);
                }
            }


            // Abrir URL em nova aba se disponível
            if (eventParsed.url && eventParsed.url.trim() !== '') {
                console.log('Opening URL:', eventParsed.url);
                window.open(eventParsed.url, '_blank');
            }
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${eventParsed.phoneNumber || 'N/A'}</td>
                <td>${moment(eventParsed.timestamp).format('DD/MM/YYYY HH:mm:ss')}</td>
                <td><span class="badge ${getStatusBadgeClass(eventParsed.eventType)}">${getStatusText(eventParsed.eventType)}</span></td>
                <td>${eventParsed.deviceId || 'N/A'}</td>
            `;

            const monitorTable = document.getElementById('monitorTable');
            if (monitorTable) {
                monitorTable.insertBefore(row, monitorTable.firstChild);
                // Manter apenas as últimas 10 linhas
                while (monitorTable.children.length > 10) {
                    monitorTable.removeChild(monitorTable.lastChild);
                }
            }
        }

        // Mostrar notificação
        const title = eventParsed.phoneNumber
            ? `Nova chamada de ${eventParsed.phoneNumber}`
            : 'Nova chamada recebida';
        showNotification(title);

        // Atualizar lista de chamadas recentes
        const recentCallsList = document.getElementById('recentCallsList');
        if (recentCallsList && eventParsed.phoneNumber) {
            const callItem = document.createElement('div');
            callItem.className = 'call-item';
            callItem.innerHTML = `
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <strong>${eventParsed.phoneNumber}</strong>
                        <br>
                        <small class="text-muted">${moment(eventParsed.timestamp).format('DD/MM/YYYY HH:mm:ss')}</small>
                    </div>
                    <span class="badge ${getStatusBadgeClass(eventParsed.eventType)}">${getStatusText(eventParsed.eventType)}</span>
                </div>
            `;
            recentCallsList.insertBefore(callItem, recentCallsList.firstChild);
            // Manter apenas os últimos 10 itens
            while (recentCallsList.children.length > 10) {
                recentCallsList.removeChild(recentCallsList.lastChild);
            }
        }

    } catch (error) {
        console.error('[WebSocket] Error processing event:', error);
    }
}

// Função para mostrar notificação
function showNotification(title) {
    console.log('[Notification] Showing notification:', title);
    const notification = document.getElementById('notification');
    if (notification) {
        notification.textContent = title;
        notification.style.display = 'block';
        notification.className = 'alert alert-info alert-dismissible fade show notification';

        // Adicionar botão de fechar
        const closeButton = document.createElement('button');
        closeButton.type = 'button';
        closeButton.className = 'btn-close';
        closeButton.setAttribute('data-bs-dismiss', 'alert');
        closeButton.setAttribute('aria-label', 'Close');
        notification.appendChild(closeButton);

        setTimeout(() => {
            notification.style.display = 'none';
        }, 5000);
    } else {
        console.warn('[Notification] Could not find notification element');
    }
}

// Funções auxiliares para formatação
function getStatusBadgeClass(status) {
    switch (status) {
        case 'ANSWERED': return 'bg-success';
        case 'MISSED': return 'bg-danger';
        case 'BUSY': return 'bg-warning';
        default: return 'bg-secondary';
    }
}

function getStatusText(status) {
    switch (status) {
        case 'ANSWERED': return 'Atendida';
        case 'MISSED': return 'Perdida';
        case 'BUSY': return 'Ocupado';
        default: return status;
    }
}

// Adicionar tratamento para quando a página for fechada
window.addEventListener('beforeunload', function () {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
});

