#!/bin/bash
# Скрипт для настройки nginx - выполните на сервере

set -e

NGINX_CONFIG=""
if [ -f "/etc/nginx/sites-available/drivebit.my" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/drivebit.my"
elif [ -f "/etc/nginx/sites-available/drivebit-clients" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/drivebit-clients"
elif [ -f "/etc/nginx/conf.d/drivebit.conf" ]; then
    NGINX_CONFIG="/etc/nginx/conf.d/drivebit.conf"
else
    echo "⚠️  Конфигурация не найдена"
    ls -la /etc/nginx/sites-available/ 2>/dev/null || true
    exit 1
fi

echo "✅ Конфигурация: $NGINX_CONFIG"
BACKUP="${NGINX_CONFIG}.backup.$(date +%Y%m%d_%H%M%S)"
cp "$NGINX_CONFIG" "$BACKUP"
echo "💾 Резервная копия: $BACKUP"

if grep -q "location /api/" "$NGINX_CONFIG"; then
    sed -i '/location \/api\//,/^[[:space:]]*}/d' "$NGINX_CONFIG"
fi

if grep -q "^[[:space:]]*location /[[:space:]]*{" "$NGINX_CONFIG"; then
    sed -i '/^[[:space:]]*location \/[[:space:]]*{/i\
    location /api/ {\
        proxy_pass http://155.212.170.94:5000/;\
        proxy_set_header Host $host;\
        proxy_set_header X-Real-IP $remote_addr;\
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\
        proxy_set_header X-Forwarded-Proto $scheme;\
    }\
' "$NGINX_CONFIG"
fi

nginx -t && systemctl reload nginx && echo "✅ Готово!"
