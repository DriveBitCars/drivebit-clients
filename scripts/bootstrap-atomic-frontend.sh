#!/usr/bin/env bash
set -euo pipefail

DEPLOY_ROOT="${DEPLOY_ROOT:-/var/www/drivebit-clients}"
BOOTSTRAP_NAME="${BOOTSTRAP_NAME:-bootstrap}"

if [[ "$(id -u)" -ne 0 ]]; then
  echo "Run as root (sudo)." >&2
  exit 1
fi

mkdir -p "$DEPLOY_ROOT/releases/$BOOTSTRAP_NAME"

shopt -s nullglob dotglob
for item in "$DEPLOY_ROOT"/*; do
  base=$(basename "$item")
  case "$base" in
    releases|current|.*) continue ;;
    drivebit-clients.backup.*) continue ;;
  esac
  if [[ -e "$item" ]]; then
    echo "Moving $item -> $DEPLOY_ROOT/releases/$BOOTSTRAP_NAME/"
    mv "$item" "$DEPLOY_ROOT/releases/$BOOTSTRAP_NAME/"
  fi
done
shopt -u nullglob dotglob

if [[ ! -f "$DEPLOY_ROOT/releases/$BOOTSTRAP_NAME/index.html" ]]; then
  echo "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Deploy pending</title></head><body></body></html>" \
    >"$DEPLOY_ROOT/releases/$BOOTSTRAP_NAME/index.html"
fi

ln -sfn "releases/$BOOTSTRAP_NAME" "$DEPLOY_ROOT/current"

echo "Bootstrap done."
echo "Symlink: $DEPLOY_ROOT/current -> $(readlink "$DEPLOY_ROOT/current")"
echo "Set nginx SPA root to: $DEPLOY_ROOT/current"
echo "Example: sed -i 's|root $DEPLOY_ROOT;|root $DEPLOY_ROOT/current;|g' /etc/nginx/sites-available/drivebit.my"
echo "Then: nginx -t && systemctl reload nginx"
