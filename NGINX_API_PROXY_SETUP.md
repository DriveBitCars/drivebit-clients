# Настройка Nginx для проксирования API на новый бэкенд

## Задача
Настроить nginx на сервере `drivebit.ru` так, чтобы `https://drivebit.ru/api/` проксировал запросы на новый бэкенд `http://155.212.170.94:5000/`

## Автоматическая настройка

Запустите скрипт:
```bash
./setup-api-proxy.sh [SSH_USER@SSH_HOST]
```

Например:
```bash
./setup-api-proxy.sh root@143.198.69.242
```

## Ручная настройка

### 1. Подключитесь к серверу
```bash
ssh root@143.198.69.242
```

### 2. Найдите конфигурацию nginx
```bash
# Проверьте возможные расположения:
ls -la /etc/nginx/sites-available/drivebit.ru
ls -la /etc/nginx/sites-available/drivebit-clients
ls -la /etc/nginx/conf.d/drivebit.conf
```

### 3. Создайте резервную копию
```bash
NGINX_CONFIG="/etc/nginx/sites-available/drivebit.ru"  # или другой путь
cp "$NGINX_CONFIG" "${NGINX_CONFIG}.backup.$(date +%Y%m%d_%H%M%S)"
```

### 4. Добавьте или обновите блок location /api/

Откройте конфигурацию nginx и добавьте/обновите следующий блок:

```nginx
# Проксирование API на новый бэкенд
# /api/... -> http://155.212.170.94:5000/...
location /api/ {
    proxy_pass http://155.212.170.94:5000/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    
    # CORS headers (если нужно)
    add_header Access-Control-Allow-Origin * always;
    add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
    add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;
    
    if ($request_method = OPTIONS) {
        return 204;
    }
}
```

**Важно**: Этот блок должен быть добавлен **перед** блоком `location /`, чтобы nginx правильно обрабатывал запросы.

### 5. Проверьте синтаксис
```bash
nginx -t
```

### 6. Перезагрузите nginx
```bash
systemctl reload nginx
```

### 7. Проверьте работу
```bash
curl -I https://drivebit.ru/api/swagger/index.html
```

## Пример полной конфигурации

```nginx
server {
    listen 443 ssl http2;
    server_name drivebit.ru;
    
    # SSL настройки...
    
    # Проксирование API на новый бэкенд
    location /api/ {
        proxy_pass http://155.212.170.94:5000/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # Статические файлы
    location / {
        root /var/www/drivebit-clients;
        try_files $uri $uri/ /index.html;
    }
}
```

## Проверка после настройки

После настройки проверьте:
1. Swagger документация: https://drivebit.ru/api/swagger/index.html
2. API endpoints работают через https://drivebit.ru/api/

## Откат изменений

Если что-то пошло не так, восстановите из резервной копии:
```bash
NGINX_CONFIG="/etc/nginx/sites-available/drivebit.ru"
BACKUP_FILE="$(ls -t ${NGINX_CONFIG}.backup.* | head -1)"
cp "$BACKUP_FILE" "$NGINX_CONFIG"
nginx -t
systemctl reload nginx
```












