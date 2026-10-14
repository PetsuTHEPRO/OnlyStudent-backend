# Decisões arquiteturais

## Rate limiting é in-memory

- Implementado em `AuthRateLimitFilter` com contador por IP no processo.
- **Limitação:** em deploy com múltiplas instâncias, o limite é por 
  instância, não global.
- **Mitigação futura:** mover para Redis/Bucket4j compartilhado se 
  escalar horizontalmente.
- **Escopo atual:** single-instance em dev; suficiente para o estágio 
  atual do projeto.