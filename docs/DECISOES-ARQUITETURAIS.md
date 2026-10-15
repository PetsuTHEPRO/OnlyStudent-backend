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