# Arquitetura do backend

## Estrutura atual

```text
src/main/java/com/sloth/OnlyStudent/
├── application/
│   ├── administrator/
│   ├── auth/
│   ├── classroom/
│   ├── material/
│   ├── profile/
│   └── support/
├── controller/
├── entities/
│   └── DTO/
├── infra/
│   └── security/
├── repository/
├── services/
├── shared/
│   └── errors/
└── OnlyStudentApplication.java
```

## Camadas e responsabilidades

- **Domínio:** ainda não existe um pacote `domain/` separado. As entidades e
  regras de domínio estão em `entities/`; várias entidades também são
  entidades JPA. Os DTOs de transporte permanecem em `entities/DTO/` por
  compatibilidade com a estrutura existente.
- **Application:** `application/` contém os casos de uso por capacidade
  (`auth`, `classroom`, `material`, `profile`, `administrator` e `support`),
  além dos mappers de respostas e conteúdo.
- **Infrastructure:** `infra/security/` contém autenticação, configuração e
  serviços de segurança. O acesso a dados está organizado separadamente em
  `repository/`, com interfaces Spring Data JPA.
- **Interface HTTP:** `controller/` define as rotas, aplica binding e
  validação de entrada, extrai principal e delega operações à aplicação.
- **Shared:** `shared/errors/` reúne exceções de aplicação e o tratamento
  global, que mantém as mensagens de erro como texto puro.
- **Legado em transição:** `services/` ainda contém serviços usados por
  algumas rotas antigas. Não foi movido nesta consolidação para evitar
  refatoração estrutural fora do escopo.

## Fluxo de uma requisição

O fluxo padrão é:

```text
HTTP request
  → controller (valida/binda a entrada e obtém a identidade autenticada)
  → use case em application/
  → repository/
  → entidade JPA em entities/
  → mapper em application/
  → DTO de resposta
  → HTTP response
```

Os controllers de perfil, materiais, administração e suporte seguem esse
padrão. Algumas rotas legadas ainda delegam a `services/`.

## Convenções

- Casos de uso ficam em `application/<capacidade>/`.
- Mappers ficam em `application/`, próximos da capacidade que os utiliza;
  mappers de resposta seguem nomes como `ClassroomResponseMapper` e
  `AdministratorResponseMapper`.
- Exceções de aplicação ficam em `shared/errors/` e são convertidas em
  respostas pelo `GlobalErrorHandler`.
- Controllers não devem retornar entidades JPA. A auditoria desta fase não
  encontrou retornos diretos de `Administrator`, `User`, `Classroom`,
  `Material`, `Student` ou `Educator`.
- DTOs de resposta evitam expor campos internos e ciclos de serialização.
- Identidade e autorização são obtidas do principal autenticado; parâmetros
  de cliente não devem substituir o ID autenticado.

## Auditorias da consolidação

- Os seis mappers atuais estão em `application/`.
- As 17 exceções customizadas em `shared/errors/` têm handlers específicos
  no `GlobalErrorHandler`; erros de validação, integridade e demais falhas
  também têm tratamento global.
- `./mvnw -q compile` concluiu com sucesso.
- `./mvnw dependency:analyze` concluiu com sucesso, mas reportou dependências
  transitivas usadas sem declaração direta e starters declarados como não
  usados pela análise estática. Como starters e dependências transitivas
  podem fornecer configuração e runtime, nenhuma dependência foi removida.
- A saída da análise não indicou ciclos; o plugin executa análise de uso de
  dependências, não uma verificação formal de ciclos arquiteturais.

## Dívida técnica conhecida

Itens mapeados durante a refatoração (Fases 0.5–4) e endereçados em fases
futuras:

### Fase 5 — Consolidação profunda

- **`services/` e `entities/` na raiz:** consolidação completa para
  `domain/` + `infrastructure/` fica para Fase 5. Atualmente ainda há
  serviços legados usados por rotas antigas.
- **Migrations formais:** o projeto usa `ddl-auto=update`. Migrar para
  Flyway ou Liquibase para versionamento controlado de schema.
- **Constraint `UNIQUE` em `Classroom.name`:** adiada (ver
  `DECISOES-ARQUITETURAIS.md`). Atualmente validado apenas no use case.
- **Duplicação `MaterialResponseDTO` ↔ `MaterialDetailsDTO`:** mesmos
  campos, ordem diferente. Consolidar em Fase 5.
- **N+1 em `Classroom.@PostLoad`:** mantido por segurança durante a
  refatoração. Medir com dados reais antes de otimizar.
- **Endpoints globais `/material/*`:** hoje globais (`POST /material/register`,
  `DELETE /material/delete/{id}`). Ideal é contextualizar em
  `/turma/{codigo}/material/*` em fase futura.

### Backlog externo (front/infra)

- Front: campo "buscar turma por código" (não existe na UI atual)
- Front: "entrar em turma" falha — investigar status HTTP
- Front: UI para excluir turma / adicionar aluno por ID
- Support: rate limit por IP aplicado a `/support/send`; contadores
  in-memory e identificação por `getRemoteAddr()` (ver
  `DECISOES-ARQUITETURAIS.md`)
- **`permitAll()` amplo em `/support/**`:** a regra de segurança libera
  qualquer POST em `/support/**`, mas o rate limit cobre apenas
  `/support/send`. Se novos endpoints forem criados, revisar a regra
  e a cobertura de rate limit.
- `/token/validate` e `/admin/**`: auditar uso em 30 dias
- Refresh token: avaliar implementação (fora do escopo atual)