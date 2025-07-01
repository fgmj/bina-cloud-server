# PBI-1: Melhorar observabilidade do sistema

## Overview
Adicionar logging estruturado às principais funcionalidades, iniciando pelo `EventoController`, para melhorar a visibilidade operacional e facilitar troubleshooting.

## Problem Statement
A aplicação possui logging inconsistente, dificultando a identificação de falhas e medição de performance.

## User Stories
* **Como** administrador, **quero** ver logs de entrada/saída e tempo de processamento das requisições **para** monitorar o sistema.

## Technical Approach
1. Utilizar `Slf4j` com mensagens chave-valor (structured) onde aplicável.
2. Instrumentar controllers e services gradualmente.
3. Garantir que exceptions sejam logadas com stacktrace.

## UX/UI Considerations
N/A – foco backend.

## Acceptance Criteria
- [ ] Logs de entrada/saída no `EventoController` com deviceId, eventType, description.
- [ ] Tempo de processamento registrado em todos os endpoints.
- [ ] Logs de sucesso/falha distinguíveis.
- [ ] PR contém testes passando e revisão de código.

## Dependencies
Nenhuma.

## Open Questions
- Formato JSON vs texto simples? (atual: texto key=value)

## Related Tasks
* [1-1 Adicionar logs estruturados no EventoController](mdc:1-1.md)

[View in Backlog](mdc:../backlog.md#user-content-1)