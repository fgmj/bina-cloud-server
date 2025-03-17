# Bina Cloud Monitor - Extensão Chrome

Extensão do Google Chrome para monitoramento em tempo real de eventos do Bina Cloud Server.

## Funcionalidades

- 🔄 Conexão WebSocket em tempo real
- 🔔 Notificações de novos eventos
- 📱 Abertura automática de URLs
- 💾 Histórico de eventos
- ⚙️ Configuração de servidor personalizada

## Instalação

1. Abra o Chrome e navegue até `chrome://extensions/`
2. Ative o "Modo do desenvolvedor" no canto superior direito
3. Clique em "Carregar sem compactação"
4. Selecione a pasta `chrome-extension` deste projeto

## Configuração

1. Clique no ícone da extensão na barra de ferramentas
2. Configure a URL do WebSocket (padrão: `ws://localhost:8080/ws`)
3. Clique em "Salvar"

## Uso

- A extensão se conectará automaticamente ao servidor WebSocket
- Eventos novos aparecerão na lista e gerarão notificações
- URLs recebidas serão abertas automaticamente em novas abas
- Use o botão "Limpar Eventos" para limpar o histórico

## Desenvolvimento

### Estrutura de Arquivos

```
chrome-extension/
├── manifest.json     # Configuração da extensão
├── popup.html       # Interface do usuário
├── popup.js        # Lógica da interface
├── background.js   # Service worker e WebSocket
├── images/         # Ícones
└── README.md       # Este arquivo
```

### Permissões Necessárias

- `notifications`: Para mostrar notificações
- `tabs`: Para abrir URLs em novas abas
- `storage`: Para salvar configurações
- `host_permissions`: Para conexão WebSocket

## Troubleshooting

### Problemas de Conexão

1. Verifique se o servidor está rodando
2. Confirme se a URL do WebSocket está correta
3. Verifique os logs no DevTools da extensão

### Notificações não Aparecem

1. Verifique as permissões do Chrome para notificações
2. Reinicie o Chrome
3. Reinstale a extensão

## Contribuindo

1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/sua-feature`)
3. Commit suas mudanças (`git commit -am 'Add: sua feature'`)
4. Push para a branch (`git push origin feature/sua-feature`)
5. Crie um Pull Request 