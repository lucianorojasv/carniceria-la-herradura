#!/usr/bin/env bash
set -euo pipefail
mkdir -p backups
STAMP=$(date +%Y%m%d_%H%M%S)
docker compose exec -T db pg_dump -U "${POSTGRES_USER:-carniceria}" "${POSTGRES_DB:-carniceria}" | gzip > "backups/carniceria_${STAMP}.sql.gz"
echo "Backup creado en backups/carniceria_${STAMP}.sql.gz"
