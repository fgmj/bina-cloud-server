// Configuração inicial
document.addEventListener('DOMContentLoaded', function() {
    // Configurar momento.js para português
    moment.locale('pt-br');
    
    // Inicializar componentes
    setupDarkMode();
    setupCurrentTime();
    initializeCharts();
    setupWebSocket();
    loadInitialData();
});

// Configuração do modo escuro
function setupDarkMode() {
    const darkModeSwitch = document.getElementById('darkModeSwitch');
    const isDarkMode = localStorage.getItem('darkMode') === 'true';
    
    darkModeSwitch.checked = isDarkMode;
    document.body.classList.toggle('dark-mode', isDarkMode);
    
    darkModeSwitch.addEventListener('change', function() {
        document.body.classList.toggle('dark-mode');
        localStorage.setItem('darkMode', this.checked);
        updateChartsTheme(this.checked);
    });
}

// Atualização do horário atual
function setupCurrentTime() {
    function updateTime() {
        const currentTime = document.getElementById('currentTime');
        currentTime.textContent = moment().format('DD/MM/YYYY HH:mm:ss');
    }
    
    updateTime();
    setInterval(updateTime, 1000);
}

// Configuração do WebSocket
function setupWebSocket() {
    const ws = new WebSocket('wss://bina.fernandojunior.com.br/ws');
    
    ws.onopen = () => {
        console.log('Conexão WebSocket estabelecida');
        updateConnectionStatus(true);
    };
    
    ws.onclose = () => {
        console.log('Conexão WebSocket fechada');
        updateConnectionStatus(false);
        setTimeout(() => setupWebSocket(), 5000); // Tentar reconectar após 5 segundos
    };
    
    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);
        handleWebSocketMessage(data);
    };
}

// Atualização do status de conexão
function updateConnectionStatus(isConnected) {
    const statusElement = document.getElementById('connectionStatus');
    statusElement.textContent = isConnected ? 'Conectado' : 'Desconectado';
    statusElement.className = `status ${isConnected ? 'connected' : 'disconnected'}`;
}

// Inicialização dos gráficos
function initializeCharts() {
    // Gráfico de chamadas por hora
    const callsPerHourOptions = {
        series: [{
            name: 'Chamadas',
            data: Array(24).fill(0)
        }],
        chart: {
            type: 'area',
            height: 250,
            toolbar: { show: false }
        },
        xaxis: {
            categories: Array.from({length: 24}, (_, i) => `${i}h`),
            title: { text: 'Hora do dia' }
        },
        yaxis: {
            title: { text: 'Número de chamadas' }
        },
        stroke: { curve: 'smooth' },
        fill: {
            type: 'gradient',
            gradient: {
                shadeIntensity: 1,
                opacityFrom: 0.7,
                opacityTo: 0.3
            }
        }
    };
    
    const callsPerHourChart = new ApexCharts(
        document.querySelector('#callsPerHourChart'),
        callsPerHourOptions
    );
    callsPerHourChart.render();
    
    // Gráfico de distribuição por dispositivo
    const deviceDistributionOptions = {
        series: [],
        chart: {
            type: 'donut',
            height: 300
        },
        labels: [],
        responsive: [{
            breakpoint: 480,
            options: {
                chart: {
                    width: 200
                },
                legend: {
                    position: 'bottom'
                }
            }
        }]
    };
    
    const deviceDistributionChart = new ApexCharts(
        document.querySelector('#deviceDistributionChart'),
        deviceDistributionOptions
    );
    deviceDistributionChart.render();
    
    // Heatmap de horários de pico
    const peakHoursOptions = {
        series: [
            {
                name: 'Segunda',
                data: Array(24).fill(0)
            },
            {
                name: 'Terça',
                data: Array(24).fill(0)
            },
            {
                name: 'Quarta',
                data: Array(24).fill(0)
            },
            {
                name: 'Quinta',
                data: Array(24).fill(0)
            },
            {
                name: 'Sexta',
                data: Array(24).fill(0)
            }
        ],
        chart: {
            height: 300,
            type: 'heatmap'
        },
        dataLabels: {
            enabled: false
        },
        xaxis: {
            categories: Array.from({length: 24}, (_, i) => `${i}h`)
        }
    };
    
    const peakHoursChart = new ApexCharts(
        document.querySelector('#peakHoursHeatmap'),
        peakHoursOptions
    );
    peakHoursChart.render();
    
    // Análise temporal
    const temporalAnalysisOptions = {
        series: [{
            name: 'Chamadas',
            data: []
        }],
        chart: {
            type: 'bar',
            height: 300,
            toolbar: { show: false }
        },
        xaxis: {
            type: 'datetime'
        },
        yaxis: {
            title: { text: 'Número de chamadas' }
        }
    };
    
    const temporalAnalysisChart = new ApexCharts(
        document.querySelector('#temporalAnalysisChart'),
        temporalAnalysisOptions
    );
    temporalAnalysisChart.render();
    
    // Armazenar referências dos gráficos
    window.dashboardCharts = {
        callsPerHour: callsPerHourChart,
        deviceDistribution: deviceDistributionChart,
        peakHours: peakHoursChart,
        temporalAnalysis: temporalAnalysisChart
    };
}

// Carregar dados iniciais
function loadInitialData() {
    fetch('/api/calls/stats')
        .then(response => response.json())
        .then(data => {
            updateDashboardStats(data);
        })
        .catch(error => {
            console.error('Erro ao carregar dados:', error);
            addAlert('Erro ao carregar dados iniciais', 'error');
        });
}

// Atualizar estatísticas do dashboard
function updateDashboardStats(data) {
    // Atualizar métricas gerais
    document.getElementById('totalCalls').textContent = data.totalCalls;
    document.getElementById('answeredCalls').textContent = data.answeredCalls;
    document.getElementById('missedCalls').textContent = data.missedCalls;
    document.getElementById('answerRate').textContent = `${data.answerRate}%`;
    
    // Atualizar gráficos
    updateCallsPerHourChart(data.callsPerHour);
    updateDeviceDistributionChart(data.deviceStats);
    updatePeakHoursChart(data.peakHours);
    updateTemporalAnalysisChart(data.temporalData);
    
    // Atualizar métricas de pico
    updatePeakMetrics(data.peakMetrics);
    
    // Atualizar lista de chamadas recentes
    updateRecentCallsList(data.recentCalls);
}

// Atualizar gráfico de chamadas por hora
function updateCallsPerHourChart(data) {
    window.dashboardCharts.callsPerHour.updateSeries([{
        name: 'Chamadas',
        data: data
    }]);
}

// Atualizar gráfico de distribuição por dispositivo
function updateDeviceDistributionChart(data) {
    window.dashboardCharts.deviceDistribution.updateOptions({
        series: data.values,
        labels: data.labels
    });
}

// Atualizar heatmap de horários de pico
function updatePeakHoursChart(data) {
    window.dashboardCharts.peakHours.updateSeries(data);
}

// Atualizar gráfico de análise temporal
function updateTemporalAnalysisChart(data) {
    window.dashboardCharts.temporalAnalysis.updateSeries([{
        name: 'Chamadas',
        data: data
    }]);
}

// Atualizar métricas de pico
function updatePeakMetrics(data) {
    document.getElementById('currentPeak').textContent = data.currentPeak;
    document.getElementById('nextPeak').textContent = data.nextPeak;
    document.getElementById('peakComparison').textContent = data.comparison;
}

// Atualizar lista de chamadas recentes
function updateRecentCallsList(calls) {
    const tbody = document.getElementById('recentCallsList');
    tbody.innerHTML = '';
    
    calls.forEach(call => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${call.phoneNumber}</td>
            <td>${moment(call.timestamp).format('DD/MM HH:mm:ss')}</td>
            <td>${call.duration || '-'}</td>
            <td>
                <span class="badge ${call.status === 'ANSWERED' ? 'bg-success' : 'bg-danger'}">
                    ${call.status === 'ANSWERED' ? 'Atendida' : 'Perdida'}
                </span>
            </td>
            <td>${call.device}</td>
            <td>
                <button class="btn btn-sm btn-primary" onclick="openPortal('${call.phoneNumber}')">
                    Abrir Portal
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Adicionar alerta
function addAlert(message, type = 'info') {
    const alertsContainer = document.getElementById('alertsList');
    const alertElement = document.createElement('div');
    alertElement.className = `alert-item ${type} fade-in`;
    alertElement.innerHTML = `
        <strong>${moment().format('HH:mm:ss')}</strong><br>
        ${message}
    `;
    
    alertsContainer.insertBefore(alertElement, alertsContainer.firstChild);
    
    // Limitar número de alertas
    if (alertsContainer.children.length > 10) {
        alertsContainer.removeChild(alertsContainer.lastChild);
    }
}

// Abrir portal
function openPortal(phoneNumber) {
    window.open(`https://portal.gasdelivery.com.br/search?phone=${phoneNumber}`, '_blank');
}

// Atualizar tema dos gráficos
function updateChartsTheme(isDarkMode) {
    const theme = {
        mode: isDarkMode ? 'dark' : 'light',
        palette: 'palette1'
    };
    
    Object.values(window.dashboardCharts).forEach(chart => {
        chart.updateOptions({
            theme: theme
        });
    });
}

// Manipular mensagens do WebSocket
function handleWebSocketMessage(data) {
    switch (data.type) {
        case 'CALL_EVENT':
            updateDashboardStats(data.stats);
            addAlert(`Nova chamada de ${data.phoneNumber}`, 'info');
            break;
            
        case 'DEVICE_STATUS':
            updateDeviceStatus(data);
            break;
            
        case 'ALERT':
            addAlert(data.message, data.alertType);
            break;
    }
}

// Atualizar status do dispositivo
function updateDeviceStatus(data) {
    const deviceStats = document.getElementById('deviceStats');
    const deviceElement = deviceStats.querySelector(`[data-device="${data.deviceId}"]`);
    
    if (deviceElement) {
        deviceElement.querySelector('.status').className = 
            `status ${data.online ? 'connected' : 'disconnected'}`;
    } else {
        const newDevice = document.createElement('div');
        newDevice.setAttribute('data-device', data.deviceId);
        newDevice.className = 'device-status mb-2';
        newDevice.innerHTML = `
            <span>${data.deviceName}</span>
            <span class="status ${data.online ? 'connected' : 'disconnected'}">
                ${data.online ? 'Online' : 'Offline'}
            </span>
        `;
        deviceStats.appendChild(newDevice);
    }
} 