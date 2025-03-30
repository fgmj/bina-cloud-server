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
    } catch (error) {
        showError('Erro ao carregar dispositivos: ' + error.message);
    }
}

async function searchDevices() {
    const searchTerm = document.getElementById('deviceSearch').value;
    try {
        const response = await fetch('/api/dispositivos');
        const devices = await response.json();
        const filteredDevices = devices.filter(device =>
            device.nome.toLowerCase().includes(searchTerm.toLowerCase()) ||
            device.identificador.toLowerCase().includes(searchTerm.toLowerCase())
        );
        displayAvailableDevices(filteredDevices);
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
            <td>${device.nomeDispositivo}</td>
            <td>${device.dispositivoId}</td>
            <td>
                <span class="badge ${device.ativo ? 'bg-success' : 'bg-danger'}">
                    ${device.ativo ? 'Ativo' : 'Inativo'}
                </span>
            </td>
            <td>${moment(device.dataAssociacao).format('DD/MM/YYYY HH:mm:ss')}</td>
            <td>
                <button class="btn btn-sm btn-outline-danger" onclick="removeDevice(${device.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function displayAvailableDevices(devices) {
    const tbody = document.getElementById('deviceTableBody');
    tbody.innerHTML = '';

    devices.forEach(device => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${device.nome}</td>
            <td>${device.identificador}</td>
            <td>
                <span class="badge ${device.ativo ? 'bg-success' : 'bg-danger'}">
                    ${device.ativo ? 'Ativo' : 'Inativo'}
                </span>
            </td>
            <td>${moment(device.ultimoAcesso).format('DD/MM/YYYY HH:mm:ss')}</td>
            <td>
                <button class="btn btn-sm btn-outline-primary" onclick="addDevice(${device.id})">
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
    loadUserDevices(userId);
    deviceModal.show();
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

        loadUserDevices(currentUserId);
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

        loadUserDevices(currentUserId);
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