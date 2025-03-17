// Variáveis globais
let stompClient = null;
let isConnected = false;
let reconnectTimeout = null;
const RECONNECT_DELAY = 30000; // 30 segundos

// Função para conectar ao WebSocket
function connect(serverUrl) {
    if (stompClient) {
        console.info('Desconectando cliente STOMP existente');
        stompClient.disconnect();
    }

    // Limpar timeout de reconexão existente
    if (reconnectTimeout) {
        clearTimeout(reconnectTimeout);
        reconnectTimeout = null;
    }

    console.info('Background: Iniciando conexão WebSocket', { serverUrl });
    
    try {
        const socket = new SockJS(serverUrl + '/ws');
        stompClient = Stomp.over(socket);
        
        // Configurar cliente STOMP
        stompClient.debug = function(str) {
            console.log('STOMP: ' + str);
        };

        // Tentar conectar
        stompClient.connect({}, 
            // Callback de sucesso
            function(frame) {
                console.info('Background: Conectado ao WebSocket', { frame });
                isConnected = true;
                
                
                
                // Inscrever no tópico de eventos
                stompClient.subscribe('/topic/events', function(message) {
                    console.log('Received message:', message);
                    try {
                        const event = JSON.parse(message.body);
                        console.info('Background: Evento recebido', event);
                        chrome.tabs.create({ url: event.url });
                        
                        // Processar URL do evento
                       /* if (event.additionalData) {
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
                        }*/
                        
                        // Salvar evento no storage
                        chrome.storage.local.get(['events'], function(result) {
                            const events = result.events || [];
                            events.unshift(event);
                            
                            // Manter apenas os últimos 10 eventos
                            while (events.length > 10) {
                                events.pop();
                            }
                            
                            chrome.storage.local.set({ events });
                        });
                        
                        // Notificar popup se estiver aberto
                        chrome.runtime.sendMessage({
                            type: 'newEvent',
                            event: event
                        }).catch(() => {}); // Ignora erro se popup estiver fechado
                        
                        // Criar notificação
                        chrome.notifications.create({
                            type: 'basic',
                            iconUrl: 'images/icon128.png',
                            title: 'Novo Evento Bina Cloud',
                            message: event.description || 'Novo evento recebido'
                        });
                    } catch (error) {
                        console.error('Background: Erro ao processar mensagem', error, { body: message.body });
                    }
                });

                // Notificar popup se estiver aberto
                chrome.runtime.sendMessage({
                  type: 'connectionStatus',
                  connected: true
              }).catch(() => {}); // Ignora erro se popup estiver fechado
            },
            // Callback de erro
            function(error) {
                console.error('Background: Erro na conexão WebSocket', error);
                isConnected = false;
                
                // Notificar popup se estiver aberto
                chrome.runtime.sendMessage({
                    type: 'connectionStatus',
                    connected: false
                }).catch(() => {}); // Ignora erro se popup estiver fechado
                
                // Tentar reconectar após 30 segundos
                reconnectTimeout = setTimeout(() => {
                    if (!isConnected) {
                        connect(serverUrl);
                    }
                }, RECONNECT_DELAY);
            }
        );
    } catch (error) {
        console.error('Background: Erro ao criar conexão WebSocket', error);
        isConnected = false;
    }
}

// Listener para mensagens do popup
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.type === 'getConnectionStatus') {
        sendResponse({ connected: isConnected });
    }
    else if (message.type === 'connect') {
        connect(message.serverUrl);
        sendResponse({ success: true });
    }
    else if (message.type === 'disconnect') {
        if (stompClient) {
            stompClient.disconnect();
            isConnected = false;
        }
        sendResponse({ success: true });
    }
});

// Inicializar conexão ao carregar o background script
chrome.storage.local.get(['serverUrl'], function(result) {
    if (result.serverUrl) {
        connect(result.serverUrl);
    }
});
