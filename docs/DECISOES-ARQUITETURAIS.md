# Decisões arquiteturais

## Rate limiting é in-memory

- Implementado em `AuthRateLimitFilter` com contador por IP no processo.
- **Limitação:** em deploy com múltiplas instâncias, o limite é por 
  instância, não global.
- **Mitigação futura:** mover para Redis/Bucket4j compartilhado se 
  escalar horizontalmente.
- **Escopo atual:** single-instance em dev; suficiente para o estágio 
  atual do projeto.

## Constraint unique em Classroom.name adiada

- Motivo: banco `school` está vazio no momento da decisão, mas ambientes
  de produção podem ter nomes duplicados históricos.
- Validação de unicidade ocorre no use case (`existsByName`) dentro de
  transação.
- Adicionar constraint formal via migration (Flyway/Liquibase) na Fase 5
  após auditoria dos dados de produção.

## Support /send — backlog de proteção

- `/support/send` permanece público nesta fase, preservando seu uso em
  formulário público.
- Antes de qualquer mudança de contrato, confirmar com o time do front
  se a rota é usada pelo formulário público.
- Se for necessário protegê-la, avaliar rate limit por IP em vez de
  exigir autenticação.

## TokenController /token/validate — legado preservado

- Endpoint existe desde antes da refatoração (Fase 4).
- Não há consumidor conhecido no backend ou frontend atuais.
- Preservado por precaução (possível cliente externo).
- **Ação futura:** auditar uso em 30 dias e descontinuar se confirmado
  como morto.