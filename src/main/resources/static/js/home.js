// Configuração do WebSocket


// Configuração do gráfico de dispositivos
let deviceChart;
let deviceChartData = {
    active: 0,
    inactive: 0
};

// Inicialização
document.addEventListener('DOMContentLoaded', function () {
    // Inicializar o gráfico de dispositivos
    const ctx = document.getElementById('deviceChart').getContext('2d');
    deviceChart = new Chart(ctx, {
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
    });

    // Carregar dados iniciais
    loadDashboardData();

    // Configurar atualização automática
    setInterval(loadDashboardData, 30000);
});

// Função para carregar dados do dashboard
async function loadDashboardData() {
    try {
        const response = await fetch('/api/dashboard/stats');
        const data = await response.json();
        updateDashboard(data);
    } catch (error) {
        console.error('Erro ao carregar dados:', error);
    }
}

// Função para atualizar o dashboard
function updateDashboard(data) {
    // Atualizar estatísticas
    document.getElementById('totalCalls').textContent = data.totalCalls;
    document.getElementById('answeredCalls').textContent = data.answeredCalls;
    document.getElementById('missedCalls').textContent = data.missedCalls;
    document.getElementById('busyCalls').textContent = data.busyCalls;
    document.getElementById('answerRate').textContent = `${data.answerRate}%`;

    // Atualizar lista de chamadas recentes
    updateRecentCalls(data.recentCalls);

    // Atualizar tabela de monitor
    updateMonitorTable(data.recentCalls);

    // Atualizar gráfico de dispositivos
    updateDeviceChart(data.activeDevices);
}

// Função para atualizar lista de chamadas recentes
function updateRecentCalls(calls) {
    const container = document.getElementById('recentCallsList');
    container.innerHTML = calls.map(call => `
        <div class="call-item">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <span class="status-badge ${getStatusClass(call.status)}"></span>
                    <strong>${call.phoneNumber}</strong>
                </div>
                <small class="text-muted">${formatDate(call.timestamp)}</small>
            </div>
        </div>
    `).join('');
}

// Função para atualizar tabela de monitor
function updateMonitorTable(calls) {
    const tbody = document.querySelector('#monitorTable tbody');
    tbody.innerHTML = calls.map(call => `
        <tr>
            <td>${call.phoneNumber}</td>
            <td>${formatDate(call.timestamp)}</td>
            <td><span class="badge ${getStatusBadgeClass(call.status)}">${call.status}</span></td>
            <td>${call.device}</td>
        </tr>
    `).join('');
}

// Função para atualizar gráfico de dispositivos
function updateDeviceChart(devices) {
    const now = new Date();
    const threeMinutesAgo = new Date(now.getTime() - 3 * 60 * 1000);

    const activeDevices = devices.filter(device => {
        const lastConnection = new Date(device.lastConnection);
        return lastConnection > threeMinutesAgo;
    });

    deviceChartData = {
        active: activeDevices.length,
        inactive: devices.length - activeDevices.length
    };

    deviceChart.data.datasets[0].data = [deviceChartData.active, deviceChartData.inactive];
    deviceChart.update();
}

// Função auxiliar para obter classe de status
function getStatusClass(status) {
    switch (status) {
        case 'ANSWERED':
            return 'status-active';
        case 'MISSED':
        case 'BUSY':
            return 'status-inactive';
        default:
            return 'status-inactive';
    }
}

// Função auxiliar para obter classe do badge de status
function getStatusBadgeClass(status) {
    switch (status) {
        case 'ANSWERED':
            return 'bg-success';
        case 'MISSED':
            return 'bg-danger';
        case 'BUSY':
            return 'bg-warning';
        default:
            return 'bg-secondary';
    }
}

// Função auxiliar para formatar data
function formatDate(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
}

// Manipuladores de eventos do WebSocket
ws.onmessage = function (event) {
    const data = JSON.parse(event.data);
    if (data.type === 'NEW_EVENT') {
        loadDashboardData();
    }
};

ws.onerror = function (error) {
    console.error('Erro na conexão WebSocket:', error);
};

ws.onclose = function () {
    console.log('Conexão WebSocket fechada. Tentando reconectar...');
    setTimeout(() => {
        window.location.reload();
    }, 5000);
}; 