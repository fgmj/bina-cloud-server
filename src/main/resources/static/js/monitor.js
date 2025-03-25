// Função para atualizar o monitor
function updateMonitor() {
    fetch('/api/dashboard/stats')
        .then(response => response.json())
        .then(data => {
            // Atualizar dispositivos ativos
            const activeDevicesTable = document.getElementById('activeDevicesTable');
            if (activeDevicesTable) {
                const tbody = activeDevicesTable.querySelector('tbody');
                tbody.innerHTML = '';

                data.activeDevices.forEach(device => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${device.id}</td>
                        <td>${device.name}</td>
                        <td>${device.type}</td>
                        <td>${device.location}</td>
                        <td>${device.lastConnection}</td>
                        <td>
                            <span class="badge bg-success">Ativo</span>
                        </td>
                    `;
                    tbody.appendChild(row);
                });
            }

            // Atualizar últimos eventos
            const recentEventsTable = document.getElementById('recentEventsTable');
            if (recentEventsTable) {
                const tbody = recentEventsTable.querySelector('tbody');
                tbody.innerHTML = '';

                data.recentCalls.forEach(event => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${event.phoneNumber}</td>
                        <td>${new Date(event.timestamp).toLocaleString()}</td>
                        <td>${event.duration}</td>
                        <td>
                            <span class="badge ${getStatusBadgeClass(event.status)}">${event.status}</span>
                        </td>
                        <td>${event.device}</td>
                    `;
                    tbody.appendChild(row);
                });
            }
        })
        .catch(error => console.error('Erro ao atualizar monitor:', error));
}

// Função para determinar a classe do badge baseado no status
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

// Atualizar a cada 30 segundos
setInterval(updateMonitor, 30000);

// Atualizar imediatamente quando a página carregar
document.addEventListener('DOMContentLoaded', updateMonitor); 