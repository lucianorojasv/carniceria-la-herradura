#!/usr/bin/env sh
set -eu
if command -v openssl >/dev/null 2>&1; then
  openssl rand -base64 64 | tr -d '\n'
  printf '\n'
else
  echo "Instala openssl o genera una clave aleatoria de 64 caracteres o más." >&2
  exit 1
fi
