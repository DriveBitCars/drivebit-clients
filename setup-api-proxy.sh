#!/bin/bash

# Скрипт для настройки nginx проксирования API на новый бэкенд
# Использование: ./setup-api-proxy.sh [SSH_USER@SSH_HOST]
# Пример: ./setup-api-proxy.sh root@143.198.69.242

SSH_TARGET="${1:-root@143.198.69.242}"
SSH_KEY=""
NEW_BACKEND="http://155.212.170.94:5000"

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
    SSH_CMD="ssh -i $SSH_KEY -o IdentitiesOnly=yes"
fi

echo "🔧 Настройка nginx проксирования для API на $SSH_TARGET"
echo "📡 Новый бэкенд: $NEW_BACKEND"
echo ""

# Подключаемся к серверу и настраиваем nginx
$SSH_CMD -o StrictHostKeyChecking=no "$SSH_TARGET" << EOF
set -e

# Находим конфигурацию для drivebit.ru
NGINX_CONFIG=""
if [ -f "/etc/nginx/sites-available/drivebit.ru" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/drivebit.ru"
elif [ -f "/etc/nginx/sites-available/drivebit.my" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/drivebit.my"
elif [ -f "/etc/nginx/sites-available/drivebit-clients" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/drivebit-clients"
elif [ -f "/etc/nginx/conf.d/drivebit.conf" ]; then
    NGINX_CONFIG="/etc/nginx/conf.d/drivebit.conf"
else
    echo "⚠️  Конфигурация nginx для drivebit.ru не найдена"
    echo "📝 Доступные конфигурации:"
    ls -la /etc/nginx/sites-available/ 2>/dev/null || true
    ls -la /etc/nginx/conf.d/ 2>/dev/null || true
    exit 1
fi

echo "✅ Найдена конфигурация: \$NGINX_CONFIG"

# Показываем текущую конфигурацию для /api/
echo "📄 Текущая конфигурация для /api/:"
if grep -q "location /api/" "\$NGINX_CONFIG"; then
    grep -A 10 "location /api/" "\$NGINX_CONFIG" || true
else
    echo "⚠️  Блок location /api/ не найден"
fi

# Создаем резервную копию
BACKUP="\${NGINX_CONFIG}.backup.\$(date +%Y%m%d_%H%M%S)"
cp "\$NGINX_CONFIG" "\$BACKUP"
echo "💾 Создана резервная копия: \$BACKUP"

# Удаляем старый блок /api/ если существует
if grep -q "location /api/" "\$NGINX_CONFIG"; then
    echo "🗑️  Удаление старого блока location /api/..."
    sed -i '/location \/api\//,/^[[:space:]]*}/d' "\$NGINX_CONFIG"
fi

# Находим место для вставки (перед location /)
if grep -q "^[[:space:]]*location /[[:space:]]*{" "\$NGINX_CONFIG"; then
    # Вставляем перед location /
    sed -i '/^[[:space:]]*location \/[[:space:]]*{/i\
    # Проксирование API на новый бэкенд\
    # /api/... -> http://155.212.170.94:5000/...\

    location /api/ {\
        proxy_pass http://155.212.170.94:5000/;\
        proxy_set_header Host \$host;\
        proxy_set_header X-Real-IP \$remote_addr;\
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;\
        proxy_set_header X-Forwarded-Proto \$scheme;\
        \
        # CORS headers (если нужно)\
        add_header Access-Control-Allow-Origin * always;\
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;\
        add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;\
        \
        if (\$request_method = OPTIONS) {\
            return 204;\
        }\
    }\
' "\$NGINX_CONFIG"
else
    # Если location / не найден, добавляем в конец server блока
    sed -i '/^[[:space:]]*server[[:space:]]*{/a\
    # Проксирование API на новый бэкенд\
    # /api/... -> http://155.212.170.94:5000/...\
    location /api/ {\
        proxy_pass http://155.212.170.94:5000/;\
        proxy_set_header Host \$host;\
        proxy_set_header X-Real-IP \$remote_addr;\
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;\
        proxy_set_header X-Forwarded-Proto \$scheme;\
        \
        # CORS headers (если нужно)\
        add_header Access-Control-Allow-Origin * always;\
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;\
        add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;\
        \
        if (\$request_method = OPTIONS) {\
            return 204;\
        }\
    }\
' "\$NGINX_CONFIG"
fi

echo "✅ Блок проксирования API добавлен в конфигурацию"

# Показываем новую конфигурацию
echo "📄 Новая конфигурация для /api/:"
grep -A 15 "location /api/" "\$NGINX_CONFIG" || true

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
    cp "\$BACKUP" "\$NGINX_CONFIG"
    exit 1
fi

echo ""
echo "✅ Настройка завершена успешно!"
echo "📋 Проверьте работу:"
echo "   curl -I https://drivebit.ru/api/swagger/index.html"
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Настройка завершена!"
    echo ""
    echo "🧪 Тест проксирования:"
    echo "   curl -I https://drivebit.ru/api/swagger/index.html"
else
    echo ""
    echo "❌ Ошибка при настройке. Проверьте вывод выше."
    exit 1
fi












