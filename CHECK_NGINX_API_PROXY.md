# Проверка настройки Nginx для проксирования API

## Проблема
В логах браузера видны запросы к `/car/` и `/Car/` без `/api/` префикса, что приводит к 404 ошибкам.

## Решение
Добавлен автоматический шаг в деплой для настройки nginx. После следующего деплоя nginx будет автоматически настроен.

## Ручная проверка (если нужно)

### 1. Подключитесь к серверу
```bash
ssh root@143.198.69.242
```

### 2. Найдите конфигурацию nginx
```bash
# Проверьте возможные расположения:
ls -la /etc/nginx/sites-available/drivebit.my
ls -la /etc/nginx/sites-available/drivebit-clients
ls -la /etc/nginx/conf.d/drivebit.conf
```

### 3. Проверьте наличие блока location /api/
```bash
grep -A 10 "location /api/" /etc/nginx/sites-available/drivebit.my
```

Должен быть блок:
```nginx
location /api/ {
    proxy_pass http://155.212.170.94:5000/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

### 4. Проверьте порядок location блоков
Блок `location /api/` должен быть **перед** блоком `location /`, иначе nginx будет обрабатывать запросы как статические файлы.

### 5. Перезагрузите nginx
```bash
nginx -t  # Проверка синтаксиса
systemctl reload nginx
```

### 6. Проверьте работу API
```bash
curl -I https://drivebit.my/api/swagger/index.html
curl https://drivebit.my/api/Car/my
```

## Что было исправлено
- Добавлен автоматический шаг в `.github/workflows/deploy.yml` для настройки nginx
- Шаг проверяет наличие конфигурации и добавляет её при необходимости
- После следующего деплоя nginx будет автоматически настроен
