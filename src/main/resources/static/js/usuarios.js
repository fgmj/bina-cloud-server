// Variáveis globais
let userModal;
let deviceModal;
let editingUserId = null;
let currentUserId = null;

// Inicialização
document.addEventListener('DOMContentLoaded', function () {
    console.log('[Usuários] Inicializando...');
    userModal = new bootstrap.Modal(document.getElementById('userModal'));
    deviceModal = new bootstrap.Modal(document.getElementById('deviceModal'));
    loadUsers();
});

// Funções de carregamento
async function loadUsers() {
    try {
        const response = await fetch('/api/usuarios');
        const users = await response.json();
        displayUsers(users);
    } catch (error) {
        showError('Erro ao carregar usuários: ' + error.message);
    }
}

async function loadUserDevices(userId) {
    try {
        const response = await fetch(`/api/usuarios-dispositivos/usuario/${userId}`);
        const devices = await response.json();
        displayUserDevices(devices);
        // Mostrar/ocultar mensagem de "nenhum dispositivo"
        document.getElementById('noAssociatedDevices').classList.toggle('d-none', devices.length > 0);
    } catch (error) {
        showError('Erro ao carregar dispositivos: ' + error.message);
    }
}

async function searchDevices() {
    const searchTerm = document.getElementById('deviceSearch').value;
    try {
        // Buscar todos os dispositivos
        const devicesResponse = await fetch('/api/dispositivos');
        const devices = await devicesResponse.json();

        // Buscar dispositivos já associados ao usuário
        const associatedDevicesResponse = await fetch(`/api/usuarios-dispositivos/usuario/${currentUserId}`);
        const associatedDevices = await associatedDevicesResponse.json();

        // Criar um Set com os IDs dos dispositivos já associados
        const associatedDeviceIds = new Set(associatedDevices.map(device => device.dispositivoId));

        // Filtrar dispositivos que não estão associados e correspondem ao termo de busca
        const filteredDevices = devices.filter(device =>
            !associatedDeviceIds.has(device.id) && // Não está associado ao usuário
            (device.nome.toLowerCase().includes(searchTerm.toLowerCase()) ||
                device.identificador.toLowerCase().includes(searchTerm.toLowerCase()))
        );

        displayAvailableDevices(filteredDevices);
        // Mostrar/ocultar mensagem de "nenhum dispositivo"
        document.getElementById('noAvailableDevices').classList.toggle('d-none', filteredDevices.length > 0);
    } catch (error) {
        showError('Erro ao buscar dispositivos: ' + error.message);
    }
}

// Funções de exibição
function displayUsers(users) {
    const tbody = document.getElementById('userTableBody');
    tbody.innerHTML = '';

    users.forEach(user => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${user.nome}</td>
            <td>${user.email}</td>
            <td>${moment(user.dataCriacao).format('DD/MM/YYYY HH:mm:ss')}</td>
            <td>${user.ultimoAcesso ? moment(user.ultimoAcesso).format('DD/MM/YYYY HH:mm:ss') : 'Nunca'}</td>
            <td>
                <span class="badge ${user.ativo ? 'bg-success' : 'bg-danger'}">
                    ${user.ativo ? 'Ativo' : 'Inativo'}
                </span>
            </td>
            <td>
                <div class="btn-group">
                    <button class="btn btn-sm btn-outline-primary" onclick="openEditModal(${user.id})">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-info" onclick="openDeviceModal(${user.id})">
                        <i class="bi bi-hdd-network"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteUser(${user.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function displayUserDevices(devices) {
    const tbody = document.getElementById('deviceTableBody');
    tbody.innerHTML = '';

    devices.forEach(device => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <div class="d-flex align-items-center">
                    <i class="bi bi-hdd-network text-primary me-2"></i>
                    <span>${device.nomeDispositivo}</span>
                </div>
            </td>
            <td>
                <span class="badge bg-secondary">${device.dispositivoId}</span>
            </td>
            <td>
                <span class="badge ${device.ativo ? 'bg-success' : 'bg-danger'}">
                    <i class="bi ${device.ativo ? 'bi-check-circle' : 'bi-x-circle'} me-1"></i>
                    ${device.ativo ? 'Ativo' : 'Inativo'}
                </span>
            </td>
            <td>
                <i class="bi bi-calendar3 me-1"></i>
                ${moment(device.dataAssociacao).format('DD/MM/YYYY HH:mm:ss')}
            </td>
            <td>
                <button class="btn btn-sm btn-outline-danger" onclick="removeDevice(${device.id})" title="Remover dispositivo">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function displayAvailableDevices(devices) {
    const tbody = document.getElementById('availableDeviceTableBody');
    tbody.innerHTML = '';

    devices.forEach(device => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <div class="d-flex align-items-center">
                    <i class="bi bi-hdd-network text-primary me-2"></i>
                    <span>${device.nome}</span>
                </div>
            </td>
            <td>
                <span class="badge bg-secondary">${device.identificador}</span>
            </td>
            <td>
                <span class="badge ${device.ativo ? 'bg-success' : 'bg-danger'}">
                    <i class="bi ${device.ativo ? 'bi-check-circle' : 'bi-x-circle'} me-1"></i>
                    ${device.ativo ? 'Ativo' : 'Inativo'}
                </span>
            </td>
            <td>
                <i class="bi bi-clock me-1"></i>
                ${moment(device.ultimoAcesso).format('DD/MM/YYYY HH:mm:ss')}
            </td>
            <td>
                <button class="btn btn-sm btn-outline-primary" onclick="addDevice(${device.id})" title="Adicionar dispositivo">
                    <i class="bi bi-plus-circle"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Funções de modal
function openCreateModal() {
    console.log('[Usuários] Abrindo modal de criação');
    editingUserId = null;
    document.getElementById('modalTitle').textContent = 'Novo Usuário';
    document.getElementById('userForm').reset();
    document.getElementById('ativo').checked = true;
    userModal.show();
}

function openEditModal(userId) {
    console.log('[Usuários] Abrindo modal de edição para usuário:', userId);
    editingUserId = userId;
    document.getElementById('modalTitle').textContent = 'Editar Usuário';

    fetch(`/api/usuarios/${userId}`)
        .then(response => response.json())
        .then(user => {
            document.getElementById('userId').value = user.id;
            document.getElementById('nome').value = user.nome;
            document.getElementById('email').value = user.email;
            document.getElementById('ativo').checked = user.ativo;
            document.getElementById('password').value = '';
        })
        .catch(error => showError('Erro ao carregar usuário: ' + error.message));

    userModal.show();
}

function openDeviceModal(userId) {
    currentUserId = userId;
    document.getElementById('deviceSearch').value = '';

    // Carregar dados iniciais
    loadUserDevices(userId);
    searchDevices();

    // Mostrar a primeira aba
    const firstTab = document.querySelector('#deviceTabs .nav-link');
    const firstTabContent = document.querySelector('#deviceTabsContent .tab-pane');
    firstTab.classList.add('active');
    firstTabContent.classList.add('show');

    deviceModal.show();
}

// Adicionar evento de busca em tempo real
document.getElementById('deviceSearch').addEventListener('input', debounce(searchDevices, 300));

// Função de debounce para limitar chamadas de API
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Funções de salvamento
async function saveUser() {
    console.log('[Usuários] Salvando usuário');
    const userData = {
        nome: document.getElementById('nome').value,
        email: document.getElementById('email').value,
        ativo: document.getElementById('ativo').checked
    };

    const password = document.getElementById('password').value;
    if (password) {
        userData.password = password;
    }

    try {
        const url = editingUserId ? `/api/usuarios/${editingUserId}` : '/api/usuarios';
        const method = editingUserId ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        if (!response.ok) {
            throw new Error('Erro ao salvar usuário');
        }

        userModal.hide();
        loadUsers();
        showSuccess(editingUserId ? 'Usuário atualizado com sucesso!' : 'Usuário criado com sucesso!');
    } catch (error) {
        showError('Erro ao salvar usuário: ' + error.message);
    }
}

async function deleteUser(userId) {
    console.log('[Usuários] Excluindo usuário:', userId);
    if (confirm('Tem certeza que deseja excluir este usuário?')) {
        try {
            const response = await fetch(`/api/usuarios/${userId}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error('Erro ao excluir usuário');
            }

            loadUsers();
            showSuccess('Usuário excluído com sucesso!');
        } catch (error) {
            showError('Erro ao excluir usuário: ' + error.message);
        }
    }
}

async function addDevice(deviceId) {
    try {
        const response = await fetch(`/api/usuarios-dispositivos/usuario/${currentUserId}/dispositivo/${deviceId}`, {
            method: 'POST'
        });

        if (!response.ok) {
            throw new Error('Erro ao adicionar dispositivo');
        }

        // Atualizar a lista de dispositivos associados
        loadUserDevices(currentUserId);
        // Atualizar a lista de dispositivos disponíveis
        searchDevices();
        showSuccess('Dispositivo adicionado com sucesso!');
    } catch (error) {
        showError('Erro ao adicionar dispositivo: ' + error.message);
    }
}

async function removeDevice(associationId) {
    if (!confirm('Tem certeza que deseja remover este dispositivo?')) {
        return;
    }

    try {
        const response = await fetch(`/api/usuarios-dispositivos/usuario/${currentUserId}/dispositivo/${associationId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('Erro ao remover dispositivo');
        }

        // Atualizar a lista de dispositivos associados
        loadUserDevices(currentUserId);
        // Atualizar a lista de dispositivos disponíveis
        searchDevices();
        showSuccess('Dispositivo removido com sucesso!');
    } catch (error) {
        showError('Erro ao remover dispositivo: ' + error.message);
    }
}

// Funções de feedback
function showSuccess(message) {
    const notification = document.getElementById('notification');
    notification.innerHTML = `
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    setTimeout(() => {
        notification.innerHTML = '';
    }, 5000);
}

function showError(message) {
    const notification = document.getElementById('notification');
    notification.innerHTML = `
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    setTimeout(() => {
        notification.innerHTML = '';
    }, 5000);
} 