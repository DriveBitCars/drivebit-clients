# Настройка Nginx для проксирования API на новый бэкенд

## Задача
Настроить nginx на сервере с `server_name` для `drivebit.ru` так, чтобы `https://drivebit.ru/api/` проксировал запросы на бэкенд `http://157.22.252.70:5000/`

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
# /api/... -> http://157.22.252.70:5000/...
location /api/ {
    proxy_pass http://157.22.252.70:5000/;
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
        proxy_pass http://157.22.252.70:5000/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # SEO: корень не индексируем отдельно — канонический URL /moskva
    location = / {
        return 301 https://drivebit.ru/moskva;
    }

    # Статические файлы
    location / {
        root /var/www/drivebit-clients/current;
        try_files $uri $uri/ /index.html;
    }
}
```

На сервере один раз (или при деплое через `scripts/setup-nginx-seo-root-redirect.sh`):

```bash
sudo bash scripts/setup-nginx-seo-root-redirect.sh
```

Блок `location = /` должен быть **перед** `location /`.

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

## Статика Web: один `index.html`

Шаблон главной страницы лежит в **корне репозитория** (`index.html`). Gradle при сборке JS копирует его в ресурсы модуля `composeApp` (`jsProcessResources`), отдельного `composeApp/src/jsMain/resources/index.html` нет.

В **nginx** для фронта важно, чтобы `root` (или `alias`) указывал на каталог **одной** выкладки, где совместно лежат `index.html`, `composeApp.js`, при необходимости wasm-файлы, `images/`, `vendor/` и т.д. Нельзя отдавать новый `composeApp.js` со старым `index.html` с другого пути.










