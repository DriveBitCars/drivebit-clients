#!/usr/bin/env bash
# Инвалидация in-memory кэша Prerender, дискового proxy_cache nginx и прогрев URL под UA бота.
# Вызывается с сервера после деплоя (CI копирует и запускает по SSH).
#
# Переменные окружения:
#   PRERENDER_COMPOSE_DIR — каталог с docker-compose.yml Prerender (по умолчанию /opt/drivebit-prerender)
#   PRERENDER_SERVICE     — имя сервиса в compose (по умолчанию prerender)
#   SITE_URL              — базовый URL сайта (по умолчанию https://drivebit.ru)
#   WARM_EXTRA_URLS       — доп. URL через пробел (опционально)
#   CURL_MAX_TIME         — таймаут curl в секундах (по умолчанию 120)
#   BOT_UA                — User-Agent для прогрева (по умолчанию Googlebot)

set -euo pipefail

SUDO_CMD=""
if [ "${EUID:-0}" -ne 0 ]; then
  SUDO_CMD="sudo"
fi

PRERENDER_COMPOSE_DIR="${PRERENDER_COMPOSE_DIR:-/opt/drivebit-prerender}"
PRERENDER_SERVICE="${PRERENDER_SERVICE:-prerender}"
SITE_URL="${SITE_URL:-https://drivebit.ru}"
CURL_MAX_TIME="${CURL_MAX_TIME:-120}"
BOT_UA="${BOT_UA:-Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)}"

echo "🔥 warm-prerender-after-deploy: SITE_URL=$SITE_URL"

if [ -d "$PRERENDER_COMPOSE_DIR" ] && [ -f "$PRERENDER_COMPOSE_DIR/docker-compose.yml" ] && command -v docker >/dev/null 2>&1; then
  echo "🔄 Restarting Prerender (clears in-memory cache) in $PRERENDER_COMPOSE_DIR ..."
  if (cd "$PRERENDER_COMPOSE_DIR" && docker compose restart "$PRERENDER_SERVICE"); then
    echo "✅ docker compose restart $PRERENDER_SERVICE ok"
  else
    echo "⚠️  docker compose restart failed; trying docker restart on running prerender container ..."
    id="$(docker ps -qf "name=${PRERENDER_SERVICE}" | head -1)"
    if [ -n "$id" ]; then
      docker restart "$id" && echo "✅ docker restart $id ok" || echo "⚠️  docker restart failed (non-fatal)"
    else
      echo "⚠️  No prerender container found (non-fatal)"
    fi
  fi
else
  echo "⚠️  Skip Prerender restart: missing $PRERENDER_COMPOSE_DIR/docker-compose.yml or docker (non-fatal)"
fi

# CLEAR_NGINX_PRERENDER_CACHE=0 — не очищать дисковый кэш (актуально при ежедневном ночном прогреве warm-prerender-nightly.sh)
if [ "${CLEAR_NGINX_PRERENDER_CACHE:-1}" = "1" ] && [ -d /var/cache/nginx/prerender ]; then
  echo "🧹 Clearing nginx prerender proxy_cache: /var/cache/nginx/prerender"
  $SUDO_CMD rm -rf /var/cache/nginx/prerender/*
  echo "✅ Nginx prerender cache dir cleared"
elif [ "${CLEAR_NGINX_PRERENDER_CACHE:-1}" != "1" ]; then
  echo "ℹ️  SKIP nginx prerender cache clear (CLEAR_NGINX_PRERENDER_CACHE=0)"
elif [ ! -d /var/cache/nginx/prerender ]; then
  echo "ℹ️  /var/cache/nginx/prerender not present (nginx proxy_cache may be unused)"
fi

warm_one() {
  local url="$1"
  echo "🌡️  Warming: $url"
  curl -sS -L -m "$CURL_MAX_TIME" \
    -A "$BOT_UA" \
    -o /dev/null \
    -w "   http_code=%{http_code} time_total=%{time_total}s\n" \
    "$url" || echo "   ⚠️  curl failed for $url (non-fatal)"
}

warm_one "${SITE_URL%/}/"

for extra in ${WARM_EXTRA_URLS:-}; do
  [ -n "$extra" ] || continue
  warm_one "$extra"
done

# Доп. страницы по умолчанию (если не заданы через WARM_EXTRA_URLS)
if [ -z "${WARM_EXTRA_URLS:-}" ]; then
  warm_one "${SITE_URL%/}/list-your-car.html" || true
fi

echo "✅ warm-prerender-after-deploy finished"
