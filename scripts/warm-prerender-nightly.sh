#!/usr/bin/env bash
# Ночной прогрев nginx proxy_cache для ответов Prerender: обход всех URL из sitemap под UA бота.
# Запуск по cron (см. scripts/cron-drivebit-prerender.example).
#
# Переменные окружения:
#   SITE_URL          — базовый URL сайта (по умолчанию https://drivebit.ru)
#   SITEMAP_URLS      — пробелом разделённые URL sitemap (по умолчанию sitemap.xml и sitemap-ru.xml)
#   BOT_UA            — User-Agent (по умолчанию Googlebot)
#   CURL_MAX_TIME     — таймаут curl на один URL в секундах (по умолчанию 180)
#   PAUSE_SEC         — пауза между запросами (по умолчанию 0.3)
#   EXTRA_URLS        — дополнительные URL через пробел (опционально)

set -euo pipefail

SITE_URL="${SITE_URL:-https://drivebit.ru}"
SITE_URL="${SITE_URL%/}"
DEFAULT_SITEMAPS="$SITE_URL/sitemap.xml $SITE_URL/sitemap-ru.xml"
SITEMAP_URLS="${SITEMAP_URLS:-$DEFAULT_SITEMAPS}"
BOT_UA="${BOT_UA:-Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)}"
CURL_MAX_TIME="${CURL_MAX_TIME:-180}"
PAUSE_SEC="${PAUSE_SEC:-0.3}"

tmp_list="$(mktemp)"
tmp_one="$(mktemp)"
cleanup() {
  rm -f "$tmp_list" "$tmp_one"
}
trap cleanup EXIT

echo "🌙 warm-prerender-nightly: SITE_URL=$SITE_URL"

for sm in $SITEMAP_URLS; do
  [ -n "$sm" ] || continue
  echo "📥 Fetch sitemap: $sm"
  if curl -sS -f -m 120 "$sm" -o "$tmp_one"; then
    grep -oE '<loc>[^<]+</loc>' "$tmp_one" | sed 's/<loc>//;s/<\/loc>//' >>"$tmp_list" || true
  else
    echo "   ⚠️  skip (unavailable): $sm"
  fi
done

{
  echo "$SITE_URL/"
  echo "$SITE_URL/list-your-car"
  echo "$SITE_URL/list-your-car.html"
  echo "$SITE_URL/prerender-bot.html"
  for u in ${EXTRA_URLS:-}; do
    [ -n "$u" ] || continue
    echo "$u"
  done
} >>"$tmp_list"

sort -u "$tmp_list" -o "$tmp_list"
total="$(wc -l <"$tmp_list" | tr -d ' ')"
echo "🔁 Warming $total URLs (nginx + Prerender cache)..."

n=0
while IFS= read -r url; do
  [ -n "$url" ] || continue
  n=$((n + 1))
  code="$(curl -sS -L -m "$CURL_MAX_TIME" -A "$BOT_UA" -o /dev/null -w "%{http_code}" "$url" || echo "err")"
  echo "   [$n/$total] $code  $url"
  sleep "$PAUSE_SEC"
done <"$tmp_list"

echo "✅ warm-prerender-nightly finished ($total URLs)"
