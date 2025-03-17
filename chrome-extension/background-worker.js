// Função para carregar uma biblioteca como texto
async function loadLibrary(url) {
    const response = await fetch(chrome.runtime.getURL(url));
    const text = await response.text();
    return text;
}

// Função para inicializar o worker
async function initializeWorker() {
    try {
        // Carregar as bibliotecas
        const sockjs = await loadLibrary('lib/sockjs.min.js');
        const stomp = await loadLibrary('lib/stomp.min.js');
        
        // Avaliar as bibliotecas no contexto global
        eval(sockjs);
        eval(stomp);
        
        // Carregar e avaliar o código principal
        const mainCode = await loadLibrary('background.js');
        eval(mainCode);
        
    } catch (error) {
        console.error('Erro ao inicializar worker:', error);
    }
}

// Inicializar o worker
initializeWorker(); 