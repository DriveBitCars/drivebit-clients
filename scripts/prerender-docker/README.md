# Self-hosted Prerender (Docker)

Сервис на базе [prerender/prerender](https://github.com/prerender/prerender) (MIT): Headless Chromium отдаёт HTML после выполнения JS.

## Запуск

```bash
cd scripts/prerender-docker
docker compose up -d --build
```

Слушает только `127.0.0.1:3000` (см. `docker-compose.yml`). Nginx на хосте проксирует ботов на этот порт — см. `nginx-prerender.conf.example` в корне репозитория.

**YandexBot:** в примере nginx в `map $is_bot` есть `~*yandex` — такие запросы идут в Prerender наравне с Googlebot. Кэш в памяти хранится по URL, не по UA; прогрев с Googlebot после деплоя покрывает и Яндекс. Подробнее — раздел «Prerender для роботов» в [корневом README](../../README.md).

## Проверка

```bash
curl -sS -m 120 "http://127.0.0.1:3000/https://example.com" | head
```

Автоматический smoke-тест (эквивалент `curl http://127.0.0.1:3000/https://drivebit.ru/`) после `docker compose up`:

```bash
npm test
# или явно
PRERENDER_TARGET_URL=https://drivebit.ru/ npm test
```

Переменные: `PRERENDER_BASE`, `PRERENDER_TARGET_URL`, `PRERENDER_VERIFY_TIMEOUT_MS`.

Память и CPU:

```bash
docker stats prerender-docker-prerender-1 --no-stream
```

(имя контейнера может отличаться; `docker ps` для точного имени.)

## Переменные окружения

| Переменная | Назначение |
|------------|------------|
| `PORT` | Порт внутри контейнера (по умолчанию `3000`) |
| `CHROME_LOCATION` | Путь к Chromium (по умолчанию `/usr/bin/chromium`) |
| `CHROME_EXTRA_FLAGS` | Доп. флаги через запятую |
| `PAGE_LOAD_TIMEOUT` | Таймаут загрузки страницы (мс) |
| `LOG_REQUESTS` | `true` для логов запросов |
| `CACHE_TTL` | Секунды жизни записи в in-memory кэше Prerender (по умолчанию `60`) |
| `CACHE_MAXSIZE` | Макс. число URL в памяти (по умолчанию `100`) |

Первый запрос к URL по-прежнему рендерится в Chromium (секунды). Повторные запросы с тем же URL отдаются из **prerender-memory-cache** без повторного рендера. На стороне Nginx можно дополнительно включить **proxy_cache** для ответов `@prerender_bot` — см. [`nginx-prerender-cache-http.conf.example`](../../nginx-prerender-cache-http.conf.example) и комментарии в [`nginx-prerender.conf.example`](../../nginx-prerender.conf.example).

## Инвалидация и прогрев после деплоя

После выкладки нового фронта старый HTML может оставаться в памяти Prerender и в дисковом кэше Nginx. В CI ([`.github/workflows/deploy.yml`](../../.github/workflows/deploy.yml)) после проверки `index.html` выполняется скрипт [`scripts/warm-prerender-after-deploy.sh`](../../scripts/warm-prerender-after-deploy.sh):

1. `docker compose restart prerender` в `PRERENDER_COMPOSE_DIR` (по умолчанию `/opt/drivebit-prerender`) — сброс in-memory кэша.
2. Очистка `/var/cache/nginx/prerender/*`, если каталог есть.
3. Прогрев публичных URL с User-Agent бота: главная `SITE_URL`, затем `list-your-car.html` (если не задан `WARM_EXTRA_URLS`).

Переменные окружения скрипта: `PRERENDER_COMPOSE_DIR`, `PRERENDER_SERVICE`, `SITE_URL`, `WARM_EXTRA_URLS`, `CURL_MAX_TIME`, `BOT_UA`. В workflow заданы `SITE_URL=https://drivebit.ru`, `PRERENDER_COMPOSE_DIR=/opt/drivebit-prerender`, `PRERENDER_SERVICE=prerender` (при необходимости измените в шаге **Invalidate and warm Prerender cache**). Шаг помечен `continue-on-error: true`, чтобы деплой не падал, если Prerender на сервере ещё не развёрнут.

## Лимиты

В `docker-compose.yml` заданы `deploy.resources.limits` (2G RAM, 1 CPU). При `docker compose` без Swarm Compose v2+ применяет лимиты; при необходимости задайте `mem_limit` вручную в вашей среде.
