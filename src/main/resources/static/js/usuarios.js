// Variáveis globais
let userModal;
let editingUserId = null;

// Inicialização
document.addEventListener('DOMContentLoaded', function () {
    console.log('[Usuários] Inicializando...');
    userModal = new bootstrap.Modal(document.getElementById('userModal'));
    loadUsers();
});

// Funções de carregamento
function loadUsers() {
    console.log('[Usuários] Carregando lista de usuários...');
    fetch('/api/usuarios')
        .then(response => response.json())
        .then(users => {
            console.log('[Usuários] Usuários carregados:', users);
            displayUsers(users);
        })
        .catch(error => {
            console.error('[Usuários] Erro ao carregar usuários:', error);
            showError('Erro ao carregar usuários');
        });
}

function displayUsers(users) {
    const userList = document.getElementById('userList');
    userList.innerHTML = '';

    users.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.nome}</td>
            <td>${user.email}</td>
            <td>
                <span class="badge ${user.ativo ? 'bg-success' : 'bg-danger'}">
                    ${user.ativo ? 'Ativo' : 'Inativo'}
                </span>
            </td>
            <td>${moment(user.dataCriacao).format('DD/MM/YYYY HH:mm')}</td>
            <td>${user.ultimoAcesso ? moment(user.ultimoAcesso).format('DD/MM/YYYY HH:mm') : 'Nunca'}</td>
            <td>
                <div class="btn-group">
                    <button class="btn btn-sm btn-outline-primary" onclick="openEditModal(${user.id})">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteUser(${user.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        `;
        userList.appendChild(row);
    });
}

// Funções do Modal
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
            document.getElementById('nome').value = user.nome;
            document.getElementById('email').value = user.email;
            document.getElementById('ativo').checked = user.ativo;
            userModal.show();
        })
        .catch(error => {
            console.error('[Usuários] Erro ao carregar usuário:', error);
            showError('Erro ao carregar dados do usuário');
        });
}

function saveUser() {
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

    const url = editingUserId ? `/api/usuarios/${editingUserId}` : '/api/usuarios';
    const method = editingUserId ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(userData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Erro ao salvar usuário');
            }
            userModal.hide();
            loadUsers();
            showSuccess(editingUserId ? 'Usuário atualizado com sucesso' : 'Usuário criado com sucesso');
        })
        .catch(error => {
            console.error('[Usuários] Erro ao salvar usuário:', error);
            showError('Erro ao salvar usuário');
        });
}

function deleteUser(userId) {
    console.log('[Usuários] Excluindo usuário:', userId);
    if (confirm('Tem certeza que deseja excluir este usuário?')) {
        fetch(`/api/usuarios/${userId}`, {
            method: 'DELETE'
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Erro ao excluir usuário');
                }
                loadUsers();
                showSuccess('Usuário excluído com sucesso');
            })
            .catch(error => {
                console.error('[Usuários] Erro ao excluir usuário:', error);
                showError('Erro ao excluir usuário');
            });
    }
}

// Funções de Feedback
function showSuccess(message) {
    const notification = document.createElement('div');
    notification.className = 'alert alert-success alert-dismissible fade show';
    notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    document.querySelector('.container-fluid').insertBefore(notification, document.querySelector('.card'));
    setTimeout(() => notification.remove(), 5000);
}

function showError(message) {
    const notification = document.createElement('div');
    notification.className = 'alert alert-danger alert-dismissible fade show';
    notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    document.querySelector('.container-fluid').insertBefore(notification, document.querySelector('.card'));
    setTimeout(() => notification.remove(), 5000);
} 