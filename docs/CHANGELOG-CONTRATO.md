# Changelog de contrato — API OnlyStudent

Mudanças que afetam o front são registradas aqui.

## 2026-10-14 — Fase 1 (refatoração de auth)

### Alterado
- **POST /auth/register** agora retorna `201 Created` com corpo vazio
  - Antes: `200 OK` com JWT em texto puro
  - Impacto no front: nenhum (o front ignora o corpo da resposta)
  - Motivo: JWT não faz sentido no registro; vaza token desnecessariamente

### Mantido
- Paths, capitalização de role, formato de erro (string pura) e todos 
  os demais shapes permanecem inalterados.