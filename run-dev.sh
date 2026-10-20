#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"

if [ -z "$jwt_secret" ]; then
    export jwt_secret=$(openssl rand -base64 32)
    echo "→ jwt_secret gerado"
fi

export db_url="jdbc:postgresql://localhost:55432/school"
export db_username=root
export db_password=123
export cors_allowed_origins="http://localhost:5173"

docker compose -f compose.yaml -f - up -d db <<'OVERRIDE'
services:
  db:
    ports: !override
      - "55432:5432"
OVERRIDE


echo "→ Aguardando Postgres..."
for i in 1 2 3 4 5 6 7 8 9 10; do
    docker compose exec -T db pg_isready -U root -d school >/dev/null 2>&1 && break
    sleep 1
done
echo "→ Postgres OK"

# 4. Roda a aplicação
echo "→ Iniciando backend na porta 8082..."
./mvnw spring-boot:run