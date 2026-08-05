#!/usr/bin/env bash
set -euo pipefail
[ -f .env ] || cp .env.example .env
docker compose up -d --build
echo "Sistema iniciado: http://localhost"
echo "Catálogo público: http://localhost/catalogo"
