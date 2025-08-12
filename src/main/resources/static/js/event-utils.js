/**
 * EventUtils - Funções utilitárias para manipulação de eventos
 * Centraliza toda a lógica de formatação e manipulação de dados de eventos
 */

const EventUtils = {
    // Static compiled regex patterns to avoid repeated compilation
    PHONE_JSON_PATTERN: /.*"numero":\s*"([^"]+)".*/,
    DIGITS_ONLY_PATTERN: /[^0-9]/g,

    // Traduzir status para português
    translateStatus: function (type) {
        const translations = {
            'CALL_RECEIVED': 'Chamada recebida',
            'CALL_MISSED': 'Chamada perdida',
            'CALL_ANSWERED': 'Chamada atendida',
            'CALL': 'Chamada'
        };
        return translations[type] || 'Evento';
    },

    // Formatar número de telefone
    formatPhoneNumber: function (num) {
        if (!num) return '';
        num = num.replace(/\D/g, '');
        if (num.length === 11) return `(${num.slice(0, 2)}) ${num.slice(2, 7)}-${num.slice(7)}`;
        if (num.length === 10) return `(${num.slice(0, 2)}) ${num.slice(2, 6)}-${num.slice(6)}`;
        if (num.length > 2) return `(${num.slice(0, 2)}) ${num.slice(2)}`;
        return num;
    },

    // Extrair número de telefone de additionalData
    extractPhoneNumber: function (additionalData) {
        if (!additionalData) return '';

        try {
            // Tentar extrair número usando regex para JSON
            let phoneNumber = additionalData.replace(this.PHONE_JSON_PATTERN, "$1");

            // Se não encontrou no formato JSON, tentar outros padrões
            if (phoneNumber === additionalData) {
                // Tentar extrair apenas números
                phoneNumber = additionalData.replace(this.DIGITS_ONLY_PATTERN, "");
            }

            if (phoneNumber && phoneNumber !== "N/A") {
                // Remover zeros à esquerda (se o número começar com 0, ignorar o 0)
                while (phoneNumber.startsWith("0")) {
                    phoneNumber = phoneNumber.substring(1);
                }

                // Normalizar para 11 dígitos (DDD + número)
                // Só truncar se tiver mais de 11 dígitos
                if (phoneNumber.length > 11) {
                    phoneNumber = phoneNumber.substring(0, 11);
                }
                return phoneNumber;
            }
        } catch (error) {
            console.error('Erro ao extrair número:', error);
        }

        return '';
    },

    // Gerar URL do portal
    generatePortalUrl: function (phoneNumber) {
        return phoneNumber ? `https://portal.gasdelivery.com.br/secure/client/?primary_phone=${phoneNumber}` : '';
    },

    // Determinar classe CSS do tipo de evento
    getEventTypeClass: function (eventType) {
        const type = (eventType || '').toLowerCase();
        if (type.includes('error') || type.includes('fail')) return 'error';
        if (type.includes('warn')) return 'warning';
        return '';
    },

    // Formatar timestamp
    formatTimestamp: function (timestamp) {
        if (!timestamp) return 'N/A';
        // O backend já envia o timestamp formatado em Brasília
        return timestamp;
    },

    // Criar card de evento reutilizável
    createEventCard: function (event, isRealtime = false) {
        const element = document.createElement('div');
        element.className = `event-item ${isRealtime ? 'realtime' : 'historical'}`;

        // Extract phone number
        let phoneNumber = event.phoneNumber || '';
        if (!phoneNumber && event.additionalData) {
            phoneNumber = this.extractPhoneNumber(event.additionalData);
        }

        // Generate portal URL
        const portalUrl = this.generatePortalUrl(phoneNumber);

        // Determine event type class
        const typeClass = this.getEventTypeClass(event.eventType);
        if (typeClass) {
            element.classList.add(typeClass);
        }

        element.setAttribute('data-device-id', event.deviceId || '');

        element.innerHTML = `
            <div class="event-title">${this.translateStatus(event.eventType)}</div>
            <div class="event-meta">                
                <span class="event-time">
                    <i class="bi bi-clock"></i>
                    ${this.formatTimestamp(event.timestamp)}
                </span>
            </div>
            <div class="event-details">
                <strong>Dispositivo:</strong> ${event.deviceId || 'N/A'}
                ${phoneNumber ? `<div class="mt-2"><strong>Número:</strong> ${this.formatPhoneNumber(phoneNumber)}</div>` : ''}
                ${event.timeSinceLastCall ? `
                    <div class="time-since-last-call">
                        <div class="label">Tempo desde última ligação</div>
                        <div class="value">${event.timeSinceLastCall}</div>
                    </div>
                ` : ''}
            </div>
            ${portalUrl ? `
                <button class="portal-button" onclick="window.open('${portalUrl}', '_blank')" title="Abrir Portal Gas Delivery">
                    <i class="bi bi-box-arrow-up-right"></i>
                </button>
            ` : ''}
        `;

        return element;
    }
};

// Exportar para uso global
window.EventUtils = EventUtils; 