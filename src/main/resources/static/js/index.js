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
        // Inicializar o gráfico de dispositivos
        const ctx = document.getElementById('deviceChart')?.getContext('2d');
        if (ctx) {
            deviceChart = new Chart(ctx, deviceChartConfig);
            console.log('[Dashboard] Device chart initialized');
        } else {
            console.warn('[Dashboard] Could not find deviceChart canvas element');
        }

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
    console.log('[WebSocket] Updating connection status:', connected ? 'Connected' : 'Disconnected');
    const statusElement = document.getElementById('connectionStatus');
    if (statusElement) {
        statusElement.textContent = connected ? 'Conectado' : 'Desconectado - Tentando reconectar...';
        statusElement.className = 'connection-status ' + (connected ? 'connected' : 'disconnected');
    } else {
        console.warn('[WebSocket] Could not find connectionStatus element');
    }
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
            if (data.deviceStats) {
                updateDeviceChart(data.deviceStats);
            } else {
                console.warn('[Dashboard] No device stats in response');
            }
            if (data.recentCalls) {
                updateRecentCalls(data.recentCalls);
                updateMonitorTable(data.recentCalls);
            } else {
                console.warn('[Dashboard] No recent calls in response');
            }
        })
        .catch(error => console.error('[Dashboard] Error loading data:', error));
}

// Função para atualizar estatísticas do dashboard
function updateDashboardStats(data) {
    console.log('[Dashboard] Updating stats:', data);
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

// Função para atualizar o gráfico de dispositivos
function updateDeviceChart(deviceStats) {
    if (deviceChart && deviceStats) {
        const activeDevices = deviceStats.values.reduce((a, b) => a + b, 0);
        deviceChart.data.datasets[0].data = [activeDevices, Math.max(0, 10 - activeDevices)];
        deviceChart.update();
    }
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
function handleNewEvent(message) {
    console.log('[WebSocket] Processing message:', message);
    eventCount++;

    try {
        // O evento pode vir diretamente ou dentro de message.event
        const event = message.event || message;
        console.log('[WebSocket] Parsed event:', event);

        // Atualizar dados do dashboard
        loadDashboardData();

        // Atualizar a tabela de monitoramento imediatamente
        if (event.phoneNumber) {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${event.phoneNumber || 'N/A'}</td>
                <td>${moment(event.timestamp).format('DD/MM/YYYY HH:mm:ss')}</td>
                <td><span class="badge ${getStatusBadgeClass(event.eventType)}">${getStatusText(event.eventType)}</span></td>
                <td>${event.deviceId || 'N/A'}</td>
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
        const title = event.phoneNumber
            ? `Nova chamada de ${event.phoneNumber}`
            : 'Nova chamada recebida';
        showNotification(title);

        // Atualizar lista de chamadas recentes
        const recentCallsList = document.getElementById('recentCallsList');
        if (recentCallsList && event.phoneNumber) {
            const callItem = document.createElement('div');
            callItem.className = 'call-item';
            callItem.innerHTML = `
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <strong>${event.phoneNumber}</strong>
                        <br>
                        <small class="text-muted">${moment(event.timestamp).format('DD/MM/YYYY HH:mm:ss')}</small>
                    </div>
                    <span class="badge ${getStatusBadgeClass(event.eventType)}">${getStatusText(event.eventType)}</span>
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

