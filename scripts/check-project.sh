#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)

required_files="
$ROOT/vercel.json
$ROOT/frontend/Dockerfile.vercel
$ROOT/backend/Dockerfile.vercel
$ROOT/backend/src/main/resources/db/migration/V1__create_schema.sql
$ROOT/backend/src/main/resources/db/migration/V2__seed_demo_data.sql
$ROOT/frontend/public/runtime-config.json
"

for file in $required_files; do
  if [ ! -f "$file" ]; then
    echo "Falta: $file" >&2
    exit 1
  fi
done

echo "Estructura Vercel + Supabase verificada."
