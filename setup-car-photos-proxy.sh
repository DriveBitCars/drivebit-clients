#!/bin/bash

# Скрипт для настройки nginx проксирования фотографий автомобилей
# Использование: ./setup-car-photos-proxy.sh [SSH_USER@SSH_HOST]
# Пример: ./setup-car-photos-proxy.sh root@143.198.69.242

SSH_TARGET="${1:-root@143.198.69.242}"
SSH_KEY=""

# Попытка найти SSH ключ
if [ -f "$(dirname "$0")/id_rsa_api_drivebit" ]; then
    SSH_KEY="$(dirname "$0")/id_rsa_api_drivebit"
elif [ -f "$(dirname "$0")/id_rsa" ]; then
    SSH_KEY="$(dirname "$0")/id_rsa"
elif [ -f "$HOME/.ssh/id_rsa_api_drivebit" ]; then
    SSH_KEY="$HOME/.ssh/id_rsa_api_drivebit"
elif [ -f "$HOME/.ssh/id_rsa" ]; then
    SSH_KEY="$HOME/.ssh/id_rsa"
fi

SSH_CMD="ssh"
if [ -n "$SSH_KEY" ]; then
    echo "Using SSH key: $SSH_KEY"
    chmod 600 "$SSH_KEY"
    SSH_CMD="ssh -i $SSH_KEY"
fi

echo "🔧 Настройка nginx проксирования для фотографий автомобилей на $SSH_TARGET"
echo ""

# Проверяем, существует ли конфигурация для drivebit.ru
echo "📋 Проверка существующей конфигурации nginx..."

$SSH_CMD -o StrictHostKeyChecking=no "$SSH_TARGET" << 'EOF'
set -e

# Находим конфигурацию для drivebit.ru или dev.drivebit.ru
NGINX_CONFIG=""
if [ -f "/etc/nginx/sites-available/dev.drivebit.ru" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/dev.drivebit.ru"
elif [ -f "/etc/nginx/sites-available/dev.drivebit.my" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/dev.drivebit.my"
elif [ -f "/etc/nginx/sites-available/drivebit.ru" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/drivebit.ru"
elif [ -f "/etc/nginx/sites-available/drivebit.my" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/drivebit.my"
elif [ -f "/etc/nginx/sites-available/drivebit-clients" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/drivebit-clients"
elif [ -f "/etc/nginx/conf.d/drivebit.conf" ]; then
    NGINX_CONFIG="/etc/nginx/conf.d/drivebit.conf"
elif [ -f "/etc/nginx/conf.d/dev.drivebit.conf" ]; then
    NGINX_CONFIG="/etc/nginx/conf.d/dev.drivebit.conf"
else
    echo "⚠️  Конфигурация nginx для drivebit.ru не найдена"
    echo "📝 Доступные конфигурации:"
    ls -la /etc/nginx/sites-available/ 2>/dev/null || true
    ls -la /etc/nginx/conf.d/ 2>/dev/null || true
    exit 1
fi

echo "✅ Найдена конфигурация: $NGINX_CONFIG"

# Проверяем, есть ли уже блок для /publicbct/
if grep -q "location /publicbct/" "$NGINX_CONFIG"; then
    echo "⚠️  Блок location /publicbct/ уже существует в конфигурации"
    echo "📄 Текущий блок:"
    grep -A 10 "location /publicbct/" "$NGINX_CONFIG" || true
    read -p "Перезаписать? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Отменено"
        exit 0
    fi
    # Удаляем старый блок
    sed -i '/location \/publicbct\//,/^[[:space:]]*}/d' "$NGINX_CONFIG"
fi

# Создаем резервную копию
BACKUP="${NGINX_CONFIG}.backup.$(date +%Y%m%d_%H%M%S)"
cp "$NGINX_CONFIG" "$BACKUP"
echo "💾 Создана резервная копия: $BACKUP"

# Находим место для вставки (перед location /)
if grep -q "^[[:space:]]*location /[[:space:]]*{" "$NGINX_CONFIG"; then
    # Вставляем перед location /
    sed -i '/^[[:space:]]*location \/[[:space:]]*{/i\
    # Проксирование фотографий автомобилей на внешний сервер\
    # /publicbct/... -> http://api.drivebit.ru:9000/publicbct/...\
    location /publicbct/ {\
        proxy_pass http://api.drivebit.ru:9000/publicbct/;\
        proxy_set_header Host api.drivebit.ru:9000;\
        proxy_set_header X-Real-IP $remote_addr;\
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\
        proxy_set_header X-Forwarded-Proto $scheme;\
        \
        # Timeouts\
        proxy_connect_timeout 60s;\
        proxy_send_timeout 60s;\
        proxy_read_timeout 60s;\
        \
        # Кеширование изображений\
        proxy_cache_valid 200 1d;\
        add_header Cache-Control "public, max-age=86400";\
    }\
' "$NGINX_CONFIG"
else
    # Если location / не найден, добавляем в конец server блока
    sed -i '/^[[:space:]]*server[[:space:]]*{/a\
    # Проксирование фотографий автомобилей на внешний сервер\
    # /publicbct/... -> http://api.drivebit.ru:9000/publicbct/...\
    location /publicbct/ {\
        proxy_pass http://api.drivebit.ru:9000/publicbct/;\
        proxy_set_header Host api.drivebit.ru:9000;\
        proxy_set_header X-Real-IP $remote_addr;\
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\
        proxy_set_header X-Forwarded-Proto $scheme;\
        \
        # Timeouts\
        proxy_connect_timeout 60s;\
        proxy_send_timeout 60s;\
        proxy_read_timeout 60s;\
        \
        # Кеширование изображений\
        proxy_cache_valid 200 1d;\
        add_header Cache-Control "public, max-age=86400";\
    }\
' "$NGINX_CONFIG"
fi

echo "✅ Блок проксирования добавлен в конфигурацию"

# Проверяем синтаксис nginx
echo "🔍 Проверка синтаксиса nginx..."
if nginx -t; then
    echo "✅ Синтаксис nginx корректен"
    echo "🔄 Перезагрузка nginx..."
    systemctl reload nginx
    echo "✅ Nginx перезагружен успешно"
else
    echo "❌ Ошибка в конфигурации nginx!"
    echo "📄 Восстановление из резервной копии..."
    cp "$BACKUP" "$NGINX_CONFIG"
    exit 1
fi

echo ""
echo "✅ Настройка завершена успешно!"
echo "📋 Проверьте работу:"
echo "   curl -I https://drivebit.ru/publicbct/cars/test.jpg"
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Настройка завершена!"
    echo ""
    echo "🧪 Тест проксирования:"
    echo "   curl -I https://drivebit.ru/publicbct/cars/test.jpg"
else
    echo ""
    echo "❌ Ошибка при настройке. Проверьте вывод выше."
    exit 1
fi
