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

## 2026-10-15 — Fase 2 (refatoração de turmas)

### Alterado
- **POST /turma/{codigo}/entrar** com código de turma inexistente agora
  retorna `404 Not Found` com `"Turma não encontrada."`
  - Antes: `400 Bad Request`
- **GET /turma/{id}** para uma turma sem vínculo de dono ou matrícula
  agora retorna `403 Forbidden` com `"Acesso negado."`
  - Antes: o detalhe da turma era acessível sem essa autorização

### Mantido
- Paths, campos JSON, `status`, `custo`, `price`, `codigo` nos detalhes,
  respostas de sucesso e erros em texto puro permanecem inalterados.