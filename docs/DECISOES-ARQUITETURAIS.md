# Decisões arquiteturais

## Rate limiting de autenticação é in-memory

- Implementado em `RateLimitFilter` com contador por IP no processo.
- **Limitação:** em deploy com múltiplas instâncias, o limite é por 
  instância, não global.
- **Mitigação futura:** mover para Redis/Bucket4j compartilhado se 
  escalar horizontalmente.
- **Escopo atual:** single-instance em dev; suficiente para o estágio 
  atual do projeto.

## Rate limit em /support/send

- Endpoint público protegido contra abuso.
- Limite: 5 requisições por hora por IP, configurável via
  `app.support.rate-limit.*`.
- **Estratégia:** `RateLimitFilter` aplica limites separados por rota sem
  alterar o limite existente de autenticação.
- **Identificação de IP:** usa `getRemoteAddr()`, não `X-Forwarded-For`.
  - Vantagem: não permite falsificação por header.
  - Limitação: atrás de proxy reverso, todos os clientes compartilham o IP
    do proxy.
  - **Ação futura:** se implantar atrás de proxy, configurar
    `server.forward-headers-strategy` e revisar.
- **Armazenamento:** in-memory, por instância.
  - Limitação: múltiplas instâncias mantêm contadores separados.
  - **Ação futura:** Redis/Bucket4j se escalar horizontalmente.
- **Resposta:** `429` + `"Muitas tentativas. Tente novamente mais tarde."`
  (texto puro, consistente com `/auth/*`).

## Constraint unique em Classroom.name adiada

- Motivo: banco `school` está vazio no momento da decisão, mas ambientes
  de produção podem ter nomes duplicados históricos.
- Validação de unicidade ocorre no use case (`existsByName`) dentro de
  transação.
- Adicionar constraint formal via migration (Flyway/Liquibase) na Fase 5
  após auditoria dos dados de produção.

## Support /send — acesso público

- `/support/send` permanece público nesta fase, preservando seu uso em
  formulário público.
- A rota tem rate limit por IP; continua sem exigir autenticação.
- O limite, estratégia de IP e limitações operacionais estão descritos na
  seção "Rate limit em /support/send".

## TokenController /token/validate — legado preservado

- Endpoint existe desde antes da refatoração (Fase 4).
- Não há consumidor conhecido no backend ou frontend atuais.
- Preservado por precaução (possível cliente externo).
- **Ação futura:** auditar uso em 30 dias e descontinuar se confirmado
  como morto.

## Endpoints de administrador preservados

- A auditoria do frontend atual não encontrou chamadas nem tela que consuma
  `/admin/**`.
- As rotas de listagem, consulta, criação, atualização, remoção e
  `admin-resource` permanecem disponíveis para possíveis consumidores
  administrativos externos ou futuros.
- As respostas mantêm o shape anterior (`id`, `name`, `telephone`,
  `email`, `role` e o alias legado `roles`), sem expor `password`.
- Reavaliar a necessidade das rotas após confirmação de consumidores.