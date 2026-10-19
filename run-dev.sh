#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"

# 1. Configura envs (gera JWT novo se não existir)
if [ -z "$JWT_SECRET" ]; then
    export JWT_SECRET=$(openssl rand -base64 32)
    echo "→ JWT_SECRET gerado"
fi

export DB_URL="jdbc:postgresql://localhost:55432/school"
export DB_USERNAME=root
export DB_PASSWORD=123

# 2. Sobe Postgres na 55432 (override)
docker compose -f compose.yaml -f - up -d db <<'OVERRIDE'
services:
  db:
    ports: !override
      - "55432:5432"
OVERRIDE

# 3. Aguarda o banco
echo "→ Aguardando Postgres..."
for i in 1 2 3 4 5 6 7 8 9 10; do
    docker compose exec -T db pg_isready -U root -d school >/dev/null 2>&1 && break
    sleep 1
done
echo "→ Postgres OK"

# 4. Roda a aplicação
echo "→ Iniciando backend na porta 8082..."
./mvnw spring-boot:run
