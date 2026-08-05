#!/bin/sh
set -eu

check_required() {
  name="$1"
  eval "value=\${$name:-}"
  if [ -z "$value" ]; then
    echo "[ERROR] Falta la variable obligatoria: $name" >&2
    exit 1
  fi
  echo "[OK] $name configurada"
}

echo "[BOOT] Iniciando Carnicería La Herradura API"
echo "[BOOT] Java: $(java -version 2>&1 | head -n 1)"
echo "[BOOT] Puerto HTTP: ${PORT:-80}"
check_required DB_URL
check_required DB_USER
check_required DB_PASSWORD
check_required JWT_SECRET

echo "[BOOT] FLYWAY_ENABLED=${FLYWAY_ENABLED:-false}"
echo "[BOOT] JPA_DDL_AUTO=${JPA_DDL_AUTO:-none}"

exec java -jar /app/app.jar \
  --server.address=0.0.0.0 \
  --server.port="${PORT:-80}" \
  --spring.main.lazy-initialization=true
