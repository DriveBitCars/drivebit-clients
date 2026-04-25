#!/bin/bash

# Скрипт для настройки nginx проксирования для dev.drivebit.ru
# Настраивает все прокси аналогично drivebit.ru/publicbct/
# Использование: ./setup-dev-proxies.sh [SSH_USER@SSH_HOST]
# Пример: ./setup-dev-proxies.sh root@143.198.69.242

SSH_TARGET="${1:-root@143.198.69.242}"
SSH_KEY=""
API_UPSTREAM_HOST="${API_UPSTREAM_HOST:-api.drivebit.ru}"
NEW_BACKEND="http://${API_UPSTREAM_HOST}:5000"
MINIO_BACKEND="http://${API_UPSTREAM_HOST}:9000"

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

echo "🔧 Настройка nginx проксирования для dev.drivebit.ru на $SSH_TARGET"
echo "📡 API бэкенд: $NEW_BACKEND"
echo "📡 MinIO бэкенд: $MINIO_BACKEND"
echo ""

# Подключаемся к серверу и настраиваем nginx
$SSH_CMD -o StrictHostKeyChecking=no "$SSH_TARGET" << 'EOF'
set -e

# Находим конфигурацию для dev.drivebit.ru
NGINX_CONFIG=""
if [ -f "/etc/nginx/sites-available/dev.drivebit.ru" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/dev.drivebit.ru"
elif [ -f "/etc/nginx/sites-available/dev.drivebit.my" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/dev.drivebit.my"
elif [ -f "/etc/nginx/conf.d/dev.drivebit.conf" ]; then
    NGINX_CONFIG="/etc/nginx/conf.d/dev.drivebit.conf"
else
    echo "⚠️  Конфигурация nginx для dev.drivebit.ru не найдена"
    echo "📝 Доступные конфигурации:"
    ls -la /etc/nginx/sites-available/ 2>/dev/null || true
    ls -la /etc/nginx/conf.d/ 2>/dev/null || true
    echo ""
    echo "❌ Создайте сначала конфигурацию для dev.drivebit.ru"
    exit 1
fi

echo "✅ Найдена конфигурация: $NGINX_CONFIG"

# Показываем текущую конфигурацию
echo "📄 Текущая конфигурация:"
grep -E "location /(api|publicbct|privatebct|avatar)/" "$NGINX_CONFIG" || echo "⚠️  Прокси блоки не найдены"

# Создаем резервную копию
BACKUP="${NGINX_CONFIG}.backup.$(date +%Y%m%d_%H%M%S)"
cp "$NGINX_CONFIG" "$BACKUP"
echo "💾 Создана резервная копия: $BACKUP"

# Удаляем старые блоки прокси если существуют
echo "🗑️  Удаление старых блоков прокси..."

# Удаляем /api/
if grep -q "location /api/" "$NGINX_CONFIG"; then
    sed -i '/location \/api\//,/^[[:space:]]*}/d' "$NGINX_CONFIG"
    echo "   ✅ Удален блок /api/"
fi

# Удаляем /publicbct/
if grep -q "location /publicbct/" "$NGINX_CONFIG"; then
    sed -i '/location \/publicbct\//,/^[[:space:]]*}/d' "$NGINX_CONFIG"
    echo "   ✅ Удален блок /publicbct/"
fi

# Удаляем /avatar/
if grep -q "location /avatar/" "$NGINX_CONFIG"; then
    sed -i '/location \/avatar\//,/^[[:space:]]*}/d' "$NGINX_CONFIG"
    echo "   ✅ Удален блок /avatar/"
fi

# Удаляем /privatebct/
if grep -q "location /privatebct/" "$NGINX_CONFIG"; then
    sed -i '/location \/privatebct\//,/^[[:space:]]*}/d' "$NGINX_CONFIG"
    echo "   ✅ Удален блок /privatebct/"
fi

# Находим место для вставки (перед location /)
if grep -q "^[[:space:]]*location /[[:space:]]*{" "$NGINX_CONFIG"; then
    # Вставляем перед location /
    echo "📝 Добавление прокси блоков перед location /..."
    
    # Создаем временный файл с блоками прокси
    cat > /tmp/nginx_proxies.conf << 'PROXY_BLOCKS'
    # Проксирование API на бэкенд
    # /api/... -> http://api.drivebit.ru:5000/...
    location /api/ {
        proxy_pass http://api.drivebit.ru:5000/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin * always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;
        
        if ($request_method = OPTIONS) {
            return 204;
        }
    }

    # Проксирование MinIO (publicbct) на внешний сервер
    # /publicbct/... -> http://api.drivebit.ru:9000/publicbct/...
    location /publicbct/ {
        proxy_pass http://api.drivebit.ru:9000/publicbct/;
        proxy_set_header Host api.drivebit.ru:9000;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Кеширование изображений
        proxy_cache_valid 200 1d;
        add_header Cache-Control "public, max-age=86400";
    }

    # Проксирование MinIO (privatebct) для presigned URL документов
    # /privatebct/... -> http://api.drivebit.ru:9000/privatebct/...
    location /privatebct/ {
        proxy_pass http://api.drivebit.ru:9000/privatebct/;
        proxy_set_header Host api.drivebit.ru:9000;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Проксирование аватаров (альтернативный путь)
    # /avatar/... -> http://api.drivebit.ru:9000/publicbct/avatars/...
    location /avatar/ {
        rewrite ^/avatar/(.*)$ /publicbct/avatars/$1 break;
        proxy_pass http://api.drivebit.ru:9000;
        proxy_set_header Host api.drivebit.ru:9000;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Кеширование изображений
        proxy_cache_valid 200 1d;
        add_header Cache-Control "public, max-age=86400";
    }
PROXY_BLOCKS

    # Вставляем блоки перед location /
    sed -i '/^[[:space:]]*location \/[[:space:]]*{/r /tmp/nginx_proxies.conf' "$NGINX_CONFIG"
    rm /tmp/nginx_proxies.conf
    echo "   ✅ Прокси блоки добавлены"
else
    # Если location / не найден, добавляем в конец server блока
    echo "📝 Добавление прокси блоков в конец server блока..."
    
    cat > /tmp/nginx_proxies.conf << 'PROXY_BLOCKS'
    # Проксирование API на бэкенд
    # /api/... -> http://api.drivebit.ru:5000/...
    location /api/ {
        proxy_pass http://api.drivebit.ru:5000/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin * always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;
        
        if ($request_method = OPTIONS) {
            return 204;
        }
    }

    # Проксирование MinIO (publicbct) на внешний сервер
    # /publicbct/... -> http://api.drivebit.ru:9000/publicbct/...
    location /publicbct/ {
        proxy_pass http://api.drivebit.ru:9000/publicbct/;
        proxy_set_header Host api.drivebit.ru:9000;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Кеширование изображений
        proxy_cache_valid 200 1d;
        add_header Cache-Control "public, max-age=86400";
    }

    # Проксирование MinIO (privatebct) для presigned URL документов
    # /privatebct/... -> http://api.drivebit.ru:9000/privatebct/...
    location /privatebct/ {
        proxy_pass http://api.drivebit.ru:9000/privatebct/;
        proxy_set_header Host api.drivebit.ru:9000;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Проксирование аватаров (альтернативный путь)
    # /avatar/... -> http://api.drivebit.ru:9000/publicbct/avatars/...
    location /avatar/ {
        rewrite ^/avatar/(.*)$ /publicbct/avatars/$1 break;
        proxy_pass http://api.drivebit.ru:9000;
        proxy_set_header Host api.drivebit.ru:9000;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Кеширование изображений
        proxy_cache_valid 200 1d;
        add_header Cache-Control "public, max-age=86400";
    }
PROXY_BLOCKS

    # Вставляем блоки в конец server блока
    sed -i '/^[[:space:]]*server[[:space:]]*{/r /tmp/nginx_proxies.conf' "$NGINX_CONFIG"
    rm /tmp/nginx_proxies.conf
    echo "   ✅ Прокси блоки добавлены"
fi

echo ""
echo "✅ Блоки проксирования добавлены в конфигурацию"

# Показываем новую конфигурацию
echo ""
echo "📄 Новая конфигурация прокси:"
grep -A 5 "location /api/" "$NGINX_CONFIG" || true
echo ""
grep -A 5 "location /publicbct/" "$NGINX_CONFIG" || true
echo ""
grep -A 5 "location /privatebct/" "$NGINX_CONFIG" || true
echo ""
grep -A 5 "location /avatar/" "$NGINX_CONFIG" || true

# Проверяем синтаксис nginx
echo ""
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
echo ""
echo "📋 Проверьте работу прокси:"
echo "   curl -I https://dev.drivebit.ru/api/swagger/index.html"
echo "   curl -I https://dev.drivebit.ru/publicbct/avatars/test.jpg"
echo "   curl -I https://dev.drivebit.ru/publicbct/cars/test.jpg"
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Настройка завершена!"
    echo ""
    echo "🧪 Тест проксирования:"
    echo "   curl -I https://dev.drivebit.ru/api/swagger/index.html"
    echo "   curl -I https://dev.drivebit.ru/publicbct/avatars/test.jpg"
    echo "   curl -I https://dev.drivebit.ru/publicbct/cars/test.jpg"
else
    echo ""
    echo "❌ Ошибка при настройке. Проверьте вывод выше."
    exit 1
fi
