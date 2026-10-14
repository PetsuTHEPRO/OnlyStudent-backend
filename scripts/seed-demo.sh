#!/usr/bin/env bash
set -euo pipefail

BASE="${BASE:-http://127.0.0.1:8082}"
PASS="OnlyStudent2026!"

command -v jq >/dev/null || { echo "instale jq: sudo apt install jq"; exit 1; }

echo "==> Cadastrando professor Marina"
curl -sS -o /dev/null -w "  status=%{http_code}\n" -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email":"professor.demo@onlystudent.test",
    "telephone":"11999990001",
    "name":"Marina",
    "password":"'"$PASS"'",
    "role":"EDUCATOR",
    "especialidade":"Programação"
  }' || true

echo "==> Cadastrando aluno Lucas"
curl -sS -o /dev/null -w "  status=%{http_code}\n" -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email":"aluno.demo@onlystudent.test",
    "telephone":"11999990002",
    "name":"Lucas",
    "password":"'"$PASS"'",
    "role":"STUDENT"
  }' || true

echo "==> Login professor"
PROF_JSON=$(curl -sS -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"professor.demo@onlystudent.test","password":"'"$PASS"'"}')
PROF_TOKEN=$(printf '%s' "$PROF_JSON" | jq -r '.token')
PROF_ID=$(printf '%s' "$PROF_JSON" | jq -r '.id')
echo "  id=$PROF_ID  token=${PROF_TOKEN:0:20}..."

echo "==> Login aluno"
ALU_JSON=$(curl -sS -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"aluno.demo@onlystudent.test","password":"'"$PASS"'"}')
ALU_TOKEN=$(printf '%s' "$ALU_JSON" | jq -r '.token')
ALU_ID=$(printf '%s' "$ALU_JSON" | jq -r '.id')
echo "  id=$ALU_ID  token=${ALU_TOKEN:0:20}..."

echo "==> Criando turma 'Fundamentos de Programação'"
curl -sS -X POST "$BASE/turma/register" \
  -H "Authorization: Bearer $PROF_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\":\"Fundamentos de Programação\",
    \"description\":\"Variáveis, condicionais, funções e primeiros exercícios.\",
    \"idEducator\":$PROF_ID,
    \"price\":0
  }"
echo

echo "==> Criando turma 'Lógica de Programação'"
curl -sS -X POST "$BASE/turma/register" \
  -H "Authorization: Bearer $PROF_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\":\"Lógica de Programação\",
    \"description\":\"Turma demo de lógica de programação.\",
    \"idEducator\":$PROF_ID,
    \"price\":0
  }"
echo

echo "==> Descobrindo codigo das turmas do professor"
TURMAS=$(curl -sS "$BASE/turma/educatorTurmas?codigo=$PROF_ID&page=0&size=100" \
  -H "Authorization: Bearer $PROF_TOKEN")
echo "$TURMAS" | jq -r '.content[] | "  codigo=\(.codigo) name=\(.name)"'

TURMA1=$(printf '%s' "$TURMAS" | jq -r '.content[] | select(.name=="Fundamentos de Programação") | .codigo' | head -n1)

if [[ -z "$TURMA1" || "$TURMA1" == "null" ]]; then
  echo "!! não achei a turma 'Fundamentos de Programação'"
  exit 1
fi
echo "  -> Fundamentos codigo=$TURMA1"

echo "==> Matriculando Lucas em Fundamentos (codigo=$TURMA1)"
curl -sS -X POST "$BASE/turma/$TURMA1/entrar" \
  -H "Authorization: Bearer $ALU_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"idAluno\":\"$ALU_ID\"}"
echo

echo "==> Verificando turmas do aluno"
curl -sS "$BASE/turma/studentTurmas?codigo=$ALU_ID&page=0&size=100" \
  -H "Authorization: Bearer $ALU_TOKEN" | jq -r '.content[] | "  \(.codigo) - \(.name)"'

echo "==> OK"
