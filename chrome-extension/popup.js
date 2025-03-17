// Variáveis globais
let isConnected = false;
const DEFAULT_SERVER_URL = 'https://bina.fernandojunior.com.br';

// Inicialização quando o popup é aberto
document.addEventListener('DOMContentLoaded', function() {
  console.info('Inicializando Bina Cloud Monitor popup');
  loadSavedConfig();
  setupEventListeners();
  setupAdvancedSettings();
  
  // Verificar status da conexão no background
  chrome.runtime.sendMessage({ type: 'getConnectionStatus' }, function(response) {
    if (response) {
      updateConnectionStatus(response.connected);
    }
  });
});

// Configurar advanced settings toggle
function setupAdvancedSettings() {
  const toggle = document.querySelector('.advanced-settings-toggle');
  const content = document.querySelector('.advanced-settings-content');
  const icon = toggle.querySelector('.toggle-icon');
  
  toggle.addEventListener('click', () => {
    content.classList.toggle('visible');
    icon.textContent = content.classList.contains('visible') ? '▼' : '▶';
  });
}

// Carregar configurações salvas
function loadSavedConfig() {
  console.debug('Carregando configurações salvas');
  chrome.storage.local.get(['serverUrl', 'events'], function(result) {
    const serverUrl = result.serverUrl || DEFAULT_SERVER_URL;
    console.info('Configurações carregadas', { serverUrl });
    
    document.getElementById('serverUrl').value = serverUrl;
    document.getElementById('savedUrl').textContent = serverUrl;
    
    // Carregar eventos salvos
    if (result.events && Array.isArray(result.events)) {
      result.events.forEach(event => addEventToList(event, false));
    }
  });
}

// Configurar event listeners
function setupEventListeners() {
  console.debug('Configurando event listeners');
  
  // Listener para o botão de salvar URL
  document.getElementById('saveUrl').addEventListener('click', function() {
    const serverUrl = document.getElementById('serverUrl').value.trim();
    
    if (!serverUrl) {
      console.warn('URL do servidor vazia');
      return;
    }
    
    console.info('Salvando nova URL do servidor', { serverUrl });
    
    chrome.storage.local.set({ serverUrl }, function() {
      document.getElementById('savedUrl').textContent = serverUrl;
      // Solicitar ao background para conectar com a nova URL
      chrome.runtime.sendMessage({ 
        type: 'connect',
        serverUrl: serverUrl
      });
    });
  });
  
  // Listener para mensagens do background
  chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.type === 'connectionStatus') {
      updateConnectionStatus(message.connected);
    }
    else if (message.type === 'newEvent') {
      addEventToList(message.event, true);
    }
  });
}

// Atualizar status da conexão na UI
function updateConnectionStatus(connected) {
  isConnected = connected;
  const statusElement = document.getElementById('connectionStatus');
  
  if (connected) {
    console.info('Status da conexão atualizado: Conectado');
    statusElement.textContent = 'Conectado';
    statusElement.className = 'status connected';
  } else {
    console.info('Status da conexão atualizado: Desconectado');
    statusElement.textContent = 'Desconectado';
    statusElement.className = 'status disconnected';
  }
}

// Função para traduzir o tipo do evento
function translateEventType(type) {
  const translations = {
    'CALL_RECEIVED': 'Chamada Recebida',
    'CALL_MISSED': 'Chamada Perdida',
    'CALL_ENDED': 'Chamada Finalizada',
    // Adicione mais traduções conforme necessário
  };
  return translations[type] || type;
}

// Adicionar evento à lista
function addEventToList(event, save = true) {
  const eventList = document.getElementById('eventList');
  const eventCount = eventList.children.length;
  
  console.debug('Adicionando evento à lista', { 
    eventCount, 
    maxEvents: 10 
  });

  // Criar elemento do evento
  const eventElement = document.createElement('div');
  eventElement.className = 'event';
  
  // Formatar data e hora
  const time = new Date().toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });

  // Extrair número do telefone do additionalData
  let phoneNumber = 'N/A';
  if (event.additionalData) {
    try {
      const additionalData = JSON.parse(event.additionalData);
      if (additionalData.numero) {
        phoneNumber = additionalData.numero;
      }
    } catch (error) {
      console.warn('Erro ao extrair número do telefone', error);
    }
  }
  
  // Construir HTML do evento
  let html = `
    <strong>📞 ${phoneNumber}</strong><br>
    <small>${event.description || 'Sem descrição'}</small><br>
    <small>Device: ${event.deviceId || 'N/A'}</small><br>
    <small>Tipo: ${translateEventType(event.eventType) || 'N/A'}</small><br>
    <small>Data: ${time}</small>
  `;
  
  // Adicionar link se houver URL
  if (event.url) {
    html += `<br><small><a href="#" onclick="chrome.tabs.create({url: '${event.url}'})">Abrir no portal</a></small>`;
  }
  
  eventElement.innerHTML = html;

  // Adicionar no início da lista
  eventList.insertBefore(eventElement, eventList.firstChild);

  // Manter apenas os últimos 10 eventos
  while (eventList.children.length > 10) {
    console.debug('Removendo evento antigo');
    eventList.removeChild(eventList.lastChild);
  }

  // Salvar eventos na storage local
  if (save) {
    const events = Array.from(eventList.children).map(el => ({
      description: el.querySelectorAll('small')[0].textContent,
      deviceId: el.querySelectorAll('small')[1].textContent.replace('Device: ', ''),
      eventType: event.eventType, // Mantém o tipo original ao salvar
      url: el.querySelector('a')?.getAttribute('onclick')?.match(/url: '(.+?)'/)?.[1],
      additionalData: event.additionalData // Mantém o additionalData original
    }));

    chrome.storage.local.set({ events }, function() {
      console.debug('Eventos salvos com sucesso', { eventCount: events.length });
    });
  }
} 