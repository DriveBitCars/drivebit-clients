#!/bin/bash
# Команды для выполнения в консоли сервера
# Скопируйте и вставьте в консоль сервера

set -e

echo "🔧 Настройка nginx для проксирования /api/ на новый бэкенд..."

# Находим конфигурацию
NGINX_CONFIG=""
if [ -f "/etc/nginx/sites-available/drivebit.my" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/drivebit.my"
elif [ -f "/etc/nginx/sites-available/drivebit-clients" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/drivebit-clients"
elif [ -f "/etc/nginx/conf.d/drivebit.conf" ]; then
    NGINX_CONFIG="/etc/nginx/conf.d/drivebit.conf"
else
    echo "⚠️  Конфигурация не найдена"
    echo "Доступные конфигурации:"
    ls -la /etc/nginx/sites-available/ 2>/dev/null || true
    ls -la /etc/nginx/conf.d/ 2>/dev/null || true
    exit 1
fi

echo "✅ Конфигурация: $NGINX_CONFIG"

# Резервная копия
BACKUP="${NGINX_CONFIG}.backup.$(date +%Y%m%d_%H%M%S)"
cp "$NGINX_CONFIG" "$BACKUP"
echo "💾 Резервная копия: $BACKUP"

# Удаляем старый блок /api/ если есть
if grep -q "location /api/" "$NGINX_CONFIG"; then
    echo "🗑️  Удаление старого блока location /api/..."
    sed -i '/location \/api\//,/^[[:space:]]*}/d' "$NGINX_CONFIG"
fi

# Добавляем новый блок /api/ перед location /
if grep -q "^[[:space:]]*location /[[:space:]]*{" "$NGINX_CONFIG"; then
    echo "➕ Добавление блока /api/ перед location /..."
    sed -i '/^[[:space:]]*location \/[[:space:]]*{/i\
    # Проксирование API на новый бэкенд\
    location /api/ {\
        proxy_pass http://155.212.170.94:5000/api/;\
        proxy_set_header Host $host;\
        proxy_set_header X-Real-IP $remote_addr;\
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\
        proxy_set_header X-Forwarded-Proto $scheme;\
    }\
' "$NGINX_CONFIG"
else
    echo "➕ Добавление блока /api/ в конец server блока..."
    sed -i '/^[[:space:]]*server[[:space:]]*{/a\
    # Проксирование API на новый бэкенд\
    location /api/ {\
        proxy_pass http://155.212.170.94:5000/api/;\
        proxy_set_header Host $host;\
        proxy_set_header X-Real-IP $remote_addr;\
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\
        proxy_set_header X-Forwarded-Proto $scheme;\
    }\
' "$NGINX_CONFIG"
fi

echo "✅ Блок /api/ добавлен"

# Показываем добавленный блок
echo "📄 Добавленная конфигурация:"
grep -A 7 "location /api/" "$NGINX_CONFIG" || true

# Проверяем синтаксис
echo "🔍 Проверка синтаксиса nginx..."
if nginx -t; then
    echo "✅ Синтаксис корректен"
    echo "🔄 Перезагрузка nginx..."
    systemctl reload nginx
    echo "✅ Nginx перезагружен"
    echo ""
    echo "✅ Настройка завершена!"
    echo "🧪 Проверьте: curl -I https://drivebit.my/api/swagger/index.html"
else
    echo "❌ Ошибка в конфигурации!"
    echo "📄 Восстановление из резервной копии..."
    cp "$BACKUP" "$NGINX_CONFIG"
    exit 1
fi












